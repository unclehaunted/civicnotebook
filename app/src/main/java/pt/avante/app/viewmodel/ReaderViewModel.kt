package pt.avante.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleBlockSerializer
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
    data class Error(val message: String) : ReaderUiState()
    data class SubscriptionRequired(val title: String) : ReaderUiState()
}

class ReaderViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()
    
    private val _readerMode = MutableStateFlow(ReaderMode.READ_MODE)
    val readerMode: StateFlow<ReaderMode> = _readerMode.asStateFlow()

    private val _articleHighlights = MutableStateFlow<List<pt.avante.app.data.db.HighlightEntity>>(emptyList())
    val articleHighlights: StateFlow<List<pt.avante.app.data.db.HighlightEntity>> = _articleHighlights.asStateFlow()

    private val _selectedPassageIndex = MutableStateFlow<Int?>(null)
    val selectedPassageIndex: StateFlow<Int?> = _selectedPassageIndex.asStateFlow()
    
    private val _selectedHighlight = MutableStateFlow<pt.avante.app.data.db.HighlightEntity?>(null)
    val selectedHighlight: StateFlow<pt.avante.app.data.db.HighlightEntity?> = _selectedHighlight.asStateFlow()
    
    var currentArticleId: String = ""
        private set

    fun loadArticle(articleSummary: ArticleSummary, editionNumber: String) {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.LoadingWithPreview(articleSummary, editionNumber)
            
            // Stable ID generation: sha256(canonicalUrl).take(16)
            val articleId = generateStableId(articleSummary.url)
            currentArticleId = articleId

            try {
                // 1. Check local cache
                val localContent = db.articleDao().getCachedContent(articleId)
                
                if (localContent != null) {
                    val blocks = ArticleBlockSerializer.deserialize(localContent.blocksJson)
                    val article = Article(
                        id = articleId,
                        title = articleSummary.title,
                        author = articleSummary.author,
                        publishedAt = "Hoje",
                        canonicalUrl = articleSummary.url,
                        publication = Publication.AVANTE,
                        heroImageUrl = articleSummary.heroImageUrl,
                        summary = articleSummary.summary,
                        blocks = blocks
                    )
                    _articleHighlights.value = db.articleDao().getHighlightsForArticle(articleId)
                    _uiState.value = ReaderUiState.Success(article)
                } else {
                    // 2. Fetch from network
                    val fetched = ArticleParser.fetchAvanteArticle(articleSummary.url, articleSummary.section)
                    if (fetched != null) {
                        // Ensure we use the stable ID
                        val stableArticle = fetched.copy(id = articleId)
                        
                        // Cache Source Stub
                        db.articleDao().insertSources(listOf(
                            ArticleEntity(
                                articleId = articleId,
                                canonicalUrl = articleSummary.url,
                                title = articleSummary.title,
                                author = articleSummary.author,
                                publication = "Avante!",
                                issueNumber = editionNumber,
                                publishedAt = editionNumber,
                                accessTier = if (articleSummary.isPremium) "PREMIUM" else "FREE",
                                summary = articleSummary.summary,
                                heroImageUrl = articleSummary.heroImageUrl
                            )
                        ))

                        // Cache Content
                        db.articleDao().insertCachedContent(
                            CachedArticleContentEntity(
                                articleId = articleId,
                                blocksJson = ArticleBlockSerializer.serialize(stableArticle.blocks),
                                plainText = null,
                                fetchedAt = System.currentTimeMillis(),
                                isOfflineCopy = false, // True explicit offline copies are dictated by Option C
                                contentLicenseStatus = "TEMPORARY"
                            )
                        )
                        _articleHighlights.value = db.articleDao().getHighlightsForArticle(articleId)
                        _uiState.value = ReaderUiState.Success(stableArticle)
                    } else {
                        _uiState.value = ReaderUiState.Error("Falha ao carregar o artigo.")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ReaderUiState.Error("Erro: ${e.message}")
            }
        }
    }
    
    fun loadArticleById(articleId: String) {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading
            currentArticleId = articleId
            try {
                val localContent = db.articleDao().getCachedContent(articleId)
                val source = db.articleDao().getSource(articleId)
                if (localContent != null && source != null) {
                    val blocks = ArticleBlockSerializer.deserialize(localContent.blocksJson)
                    val article = Article(
                        id = articleId,
                        title = source.title,
                        author = source.author ?: "",
                        publishedAt = source.publishedAt ?: "",
                        canonicalUrl = source.canonicalUrl,
                        publication = Publication.AVANTE,
                        heroImageUrl = source.heroImageUrl,
                        summary = source.summary ?: "",
                        blocks = blocks
                    )
                    _articleHighlights.value = db.articleDao().getHighlightsForArticle(articleId)
                    _uiState.value = ReaderUiState.Success(article)
                } else {
                    _uiState.value = ReaderUiState.Error("Artigo não encontrado no dispositivo.")
                }
            } catch (e: Exception) {
                _uiState.value = ReaderUiState.Error("Erro: ${e.message}")
            }
        }
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
        if (currentArticleId.isEmpty()) return null
        
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
            db.articleDao().insertNote(note)
            db.articleDao().insertNoteSource(NoteSourceEntity(noteId, currentArticleId, "NOTE_CREATED_FROM_ARTICLE"))
        }
        return noteId
    }

    fun highlightPassage(selectedText: String, blockIndex: Int, startOffset: Int? = null, endOffset: Int? = null): String? {
        if (currentArticleId.isEmpty()) return null
        
        val highlightId = UUID.randomUUID().toString()
        viewModelScope.launch {
            val highlight = pt.avante.app.data.db.HighlightEntity(
                highlightId = highlightId,
                articleId = currentArticleId,
                selectedText = selectedText,
                textBefore = null,
                textAfter = null,
                blockIndex = blockIndex,
                startOffset = startOffset,
                endOffset = endOffset,
                createdAt = System.currentTimeMillis()
            )
            db.articleDao().insertHighlight(highlight)
            _articleHighlights.value = db.articleDao().getHighlightsForArticle(currentArticleId)
        }
        clearSelection()
        return highlightId
    }

    fun selectHighlight(highlight: pt.avante.app.data.db.HighlightEntity?) {
        _selectedHighlight.value = highlight
    }

    fun removeHighlight(highlightId: String) {
        viewModelScope.launch {
            db.articleDao().deleteHighlight(highlightId)
            _articleHighlights.value = db.articleDao().getHighlightsForArticle(currentArticleId)
        }
    }

    fun commentOnPassage(selectedText: String, blockIndex: Int, startOffset: Int? = null, endOffset: Int? = null, commentBody: String) {
        val highlightId = highlightPassage(selectedText, blockIndex, startOffset, endOffset) ?: return
        viewModelScope.launch {
            val commentId = UUID.randomUUID().toString()
            val comment = pt.avante.app.data.db.HighlightCommentEntity(
                commentId = commentId,
                highlightId = highlightId,
                noteId = null,
                body = commentBody,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            db.articleDao().insertHighlightComment(comment)
            _articleHighlights.value = db.articleDao().getHighlightsForArticle(currentArticleId)
        }
    }

    private fun generateStableId(url: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }
}
