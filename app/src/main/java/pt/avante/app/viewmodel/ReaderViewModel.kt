package pt.avante.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.ArticleBlockSerializer
import pt.avante.app.data.ArticleIdentity
import pt.avante.app.data.ArticleParser
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.Publication
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.ArticleEntity
import pt.avante.app.data.db.CachedArticleContentEntity
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.NoteSourceEntity
import pt.avante.app.data.db.NoteType
import java.security.MessageDigest
import java.util.UUID

enum class ReaderMode {
    READ_MODE, ANNOTATION_MODE
}

sealed class ReaderUiState {
    data object Loading : ReaderUiState()
    data class LoadingWithPreview(val summary: ArticleSummary, val editionNumber: String) : ReaderUiState()
    data class Success(val article: Article) : ReaderUiState()
    data class Error(val reason: ReaderErrorReason) : ReaderUiState()
    data class SubscriptionRequired(val title: String) : ReaderUiState()
}

class ReaderViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()
    
    private val _readerMode = MutableStateFlow(ReaderMode.READ_MODE)
    val readerMode: StateFlow<ReaderMode> = _readerMode.asStateFlow()

    // Keyed by canonical stable ID, storing pair of (firstVisibleItemIndex, firstVisibleItemScrollOffset)
    val scrollPositions = mutableMapOf<String, Pair<Int, Int>>()

    private val _articleHighlights = MutableStateFlow<List<pt.avante.app.data.db.HighlightEntity>>(emptyList())
    val articleHighlights: StateFlow<List<pt.avante.app.data.db.HighlightEntity>> = _articleHighlights.asStateFlow()

    private val _currentArticleId = MutableStateFlow<String?>(null)
    val currentArticleId: StateFlow<String?> = _currentArticleId.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val hasArticleNotes: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        _currentArticleId,
        db.articleDao().getBookmarkedArticleIds()
    ) { articleId, bookmarks ->
        articleId != null && bookmarks.contains(articleId)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _selectedPassageIndex = MutableStateFlow<Int?>(null)
    val selectedPassageIndex: StateFlow<Int?> = _selectedPassageIndex.asStateFlow()
    
    private val _selectedHighlight = MutableStateFlow<pt.avante.app.data.db.HighlightEntity?>(null)
    val selectedHighlight: StateFlow<pt.avante.app.data.db.HighlightEntity?> = _selectedHighlight.asStateFlow()

    private var loadJob: Job? = null

    fun resetReaderState() {
        _readerMode.value = ReaderMode.READ_MODE
        _selectedPassageIndex.value = null
        _selectedHighlight.value = null
    }

    private fun beginArticleTransition(articleId: String) {
        if (_currentArticleId.value != articleId) {
            resetReaderState()
            _articleHighlights.value = emptyList()
            _selectedHighlight.value = null
        }
        _currentArticleId.value = articleId
    }

    fun loadArticle(articleSummary: ArticleSummary, editionNumber: String) {
        val articleId = ArticleIdentity.generateStableId(articleSummary.url)
        
        // If we're already loading or have successfully loaded THIS specific article, do nothing to prevent re-fetching.
        if (_currentArticleId.value == articleId && (loadJob?.isActive == true || _uiState.value is ReaderUiState.Success)) {
            return
        }

        beginArticleTransition(articleId)

        // Cancel any pending load for a different article
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            try {
                // 1. Check local cache
                val localContent = db.articleDao().getCachedContent(articleId)
                val source = db.articleDao().getSource(articleId)

                // Se o cache tem apenas o preview paywalled e há sessão ativa, ignorar cache e ir à rede
                val cachedIsPaywalled = localContent?.let {
                    ArticleBlockSerializer.deserialize(it.blocksJson).any { block -> block is ArticleBlock.PaywallCTA }
                } ?: false
                val skipCache = cachedIsPaywalled && pt.avante.app.data.auth.AvanteHttp.isLoggedIn

                if (localContent != null && !skipCache) {
                    val blocks = ArticleBlockSerializer.deserialize(localContent.blocksJson)
                    val resolvedIssueNumber = source?.issueNumber?.takeIf { it.isNotBlank() } ?: editionNumber.takeIf { it.isNotBlank() }
                    val article = if (source != null) {
                        Article(
                            id = articleId,
                            title = source.title.takeIf { it.isNotBlank() } ?: articleSummary.title,
                            author = source.author?.takeIf { it.isNotBlank() } ?: articleSummary.author,
                            publishedAt = source.publishedAt ?: "",
                            canonicalUrl = source.canonicalUrl.takeIf { it.isNotBlank() } ?: articleSummary.url,
                            publication = pt.avante.app.data.PublicationMapper.map(source.publication),
                            issueNumber = resolvedIssueNumber,
                            heroImageUrl = source.heroImageUrl,
                            summary = source.summary?.takeIf { it.isNotBlank() } ?: articleSummary.summary,
                            blocks = blocks,
                            categoryLabel = source.categoryLabel?.takeIf { it.isNotBlank() } ?: articleSummary.categoryLabel,
                            subcategoryLabel = source.subcategoryLabel?.takeIf { it.isNotBlank() } ?: articleSummary.subcategoryLabel
                        )
                    } else {
                        Article(
                            id = articleId,
                            title = articleSummary.title,
                            author = articleSummary.author,
                            publishedAt = "",
                            canonicalUrl = articleSummary.url,
                            publication = pt.avante.app.data.PublicationMapper.map(null),
                            issueNumber = resolvedIssueNumber,
                            heroImageUrl = null, // Must not reintroduce rejected feed image
                            summary = articleSummary.summary,
                            blocks = blocks,
                            categoryLabel = articleSummary.categoryLabel,
                            subcategoryLabel = articleSummary.subcategoryLabel
                        )
                    }
                    _articleHighlights.value = db.articleDao().getHighlightsForArticle(articleId)
                    _uiState.value = ReaderUiState.Success(article)
                    // Once cached content is displayed, we don't fetch from network in this pass (as per Phase B cache-first).
                } else {
                    _uiState.value = ReaderUiState.LoadingWithPreview(articleSummary, editionNumber)
                    // 2. Fetch from network
                    val fetched = ArticleParser.fetchAvanteArticle(
                        url = articleSummary.url, 
                        section = articleSummary.section,
                        heroImageFromFeed = articleSummary.heroImageUrl
                    )
                    if (fetched != null) {
                        val existing = db.articleDao().getSource(articleId)
                        val resolvedIssueNumber = existing?.issueNumber?.takeIf { it.isNotBlank() } ?: editionNumber.takeIf { it.isNotBlank() }
                        // Ensure we use the stable ID
                        val stableArticle = fetched.copy(
                            id = articleId,
                            issueNumber = resolvedIssueNumber,
                            categoryLabel = existing?.categoryLabel?.takeIf { it.isNotBlank() } ?: articleSummary.categoryLabel,
                            subcategoryLabel = existing?.subcategoryLabel?.takeIf { it.isNotBlank() } ?: articleSummary.subcategoryLabel
                        )
                        
                        // Cache Source Stub
                        db.articleDao().insertSources(listOf(
                            ArticleEntity(
                                articleId = articleId,
                                canonicalUrl = existing?.canonicalUrl?.takeIf { it.isNotBlank() } ?: articleSummary.url,
                                title = existing?.title?.takeIf { it.isNotBlank() } ?: articleSummary.title,
                                author = existing?.author?.takeIf { it.isNotBlank() } ?: articleSummary.author,
                                publication = existing?.publication?.takeIf { it.isNotBlank() } ?: "Avante!",
                                issueNumber = resolvedIssueNumber,
                                publishedAt = existing?.publishedAt,
                                accessTier = if (articleSummary.isPremium) "PREMIUM" else "FREE",
                                summary = existing?.summary?.takeIf { it.isNotBlank() } ?: articleSummary.summary,
                                heroImageUrl = fetched.heroImageUrl,
                                categoryLabel = existing?.categoryLabel?.takeIf { it.isNotBlank() } ?: articleSummary.categoryLabel,
                                subcategoryLabel = existing?.subcategoryLabel?.takeIf { it.isNotBlank() } ?: articleSummary.subcategoryLabel
                            )
                        ))

                        // Cache Content
                        db.articleDao().insertCachedContent(
                            CachedArticleContentEntity(
                                articleId = articleId,
                                blocksJson = ArticleBlockSerializer.serialize(stableArticle.blocks),
                                plainText = null,
                                fetchedAt = System.currentTimeMillis(),
                                isOfflineCopy = false,
                                contentLicenseStatus = "TEMPORARY"
                            )
                        )
                        _articleHighlights.value = db.articleDao().getHighlightsForArticle(articleId)
                        _uiState.value = ReaderUiState.Success(stableArticle)
                    } else {
                        _uiState.value = ReaderUiState.Error(ReaderErrorReason.SERVER)
                    }
                }
            } catch (e: Exception) {
                Log.e("ReaderViewModel", "Falha ao carregar artigo ${articleSummary.url}", e)
                _uiState.value = ReaderUiState.Error(e.toReaderErrorReason())
            }
        }
    }

    fun loadArticleById(articleId: String) {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading
            beginArticleTransition(articleId)
            try {
                val localContent = db.articleDao().getCachedContent(articleId)
                val source = db.articleDao().getSource(articleId)
                if (localContent != null && source != null) {
                    val blocks = ArticleBlockSerializer.deserialize(localContent.blocksJson)
                    val article = source.toArticle(blocks)
                    _articleHighlights.value = db.articleDao().getHighlightsForArticle(articleId)
                    _uiState.value = ReaderUiState.Success(article)
                } else {
                    _uiState.value = ReaderUiState.Error(ReaderErrorReason.NOT_CACHED)
                }
            } catch (e: Exception) {
                Log.e("ReaderViewModel", "Falha ao carregar artigo $articleId da cache", e)
                _uiState.value = ReaderUiState.Error(e.toReaderErrorReason())
            }
        }
    }

    fun resetReaderMode() {
        _readerMode.value = ReaderMode.READ_MODE
        _selectedPassageIndex.value = null
    }

    fun toggleAnnotationMode() {
        if (_readerMode.value == ReaderMode.READ_MODE) {
            _readerMode.value = ReaderMode.ANNOTATION_MODE
        } else {
            _readerMode.value = ReaderMode.READ_MODE
            _selectedPassageIndex.value = null
        }
    }

    fun selectPassage(blockIndex: Int) {
        if (_readerMode.value == ReaderMode.ANNOTATION_MODE) {
            _selectedPassageIndex.value = blockIndex
        }
    }

    fun clearSelection() {
        _selectedPassageIndex.value = null
    }

    fun createArticleNote(title: String, initialBody: String = "## Notas\n\nComece a escrever aqui..."): String? {
        val currentId = _currentArticleId.value
        if (currentId.isNullOrEmpty()) return null
        
        val noteId = UUID.randomUUID().toString()
        viewModelScope.launch {
            val note = NoteEntity(
                noteId = noteId,
                title = title,
                bodyMarkdown = initialBody,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                noteType = NoteType.ARTICLE_NOTE
            )
            val source = NoteSourceEntity(noteId, currentId, "NOTE_CREATED_FROM_ARTICLE")
            db.articleDao().insertNote(note)
            db.articleDao().insertNoteSource(source)
            // Trigger refresh
            _currentArticleId.value = _currentArticleId.value
        }
        return noteId
    }

    fun highlightPassage(text: String, blockIndex: Int, startOffset: Int? = null, endOffset: Int? = null) {
        val currentId = _currentArticleId.value
        if (currentId.isNullOrEmpty()) return
        
        viewModelScope.launch {
            val highlightId = UUID.randomUUID().toString()
            val highlight = pt.avante.app.data.db.HighlightEntity(
                highlightId = highlightId,
                articleId = currentId,
                selectedText = text,
                textBefore = null,
                textAfter = null,
                blockIndex = blockIndex,
                startOffset = startOffset,
                endOffset = endOffset,
                createdAt = System.currentTimeMillis()
            )
            db.articleDao().insertHighlight(highlight)
            _articleHighlights.value = db.articleDao().getHighlightsForArticle(currentId)
        }
        clearSelection()
    }

    fun selectHighlight(highlight: pt.avante.app.data.db.HighlightEntity?) {
        _selectedHighlight.value = highlight
    }

    fun removeHighlight(highlightId: String) {
        viewModelScope.launch {
            db.articleDao().deleteHighlight(highlightId)
            val currentId = _currentArticleId.value
            if (currentId != null) {
                _articleHighlights.value = db.articleDao().getHighlightsForArticle(currentId)
            }
            _selectedHighlight.value = null
        }
    }

}
