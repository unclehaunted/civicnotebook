package pt.avante.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pt.avante.app.data.ArticleParser
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.DEFAULT_ARTICLE_AUTHOR
import pt.avante.app.data.Publication
import pt.avante.app.data.HomeFeed
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.ArticleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.security.MessageDigest

import pt.avante.app.data.ArticleIdentity

class HomeViewModel(
    private val db: AppDatabase,
    private val currentIssueStore: pt.avante.app.data.CurrentIssueStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    val bookmarkedArticles: StateFlow<Set<String>> = db.articleDao().getBookmarkedArticleIds()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.Lazily,
            initialValue = emptySet()
        )

    private val _layoutMode = MutableStateFlow(pt.avante.app.ui.home.HomeLayoutMode.LIST)
    val layoutMode: StateFlow<pt.avante.app.ui.home.HomeLayoutMode> = _layoutMode.asStateFlow()

    fun setLayoutMode(mode: pt.avante.app.ui.home.HomeLayoutMode) {
        _layoutMode.value = mode
    }

    private var loadJob: Job? = null

    // Store feed scroll position keyed by layout mode
    val feedPositions = mutableMapOf<pt.avante.app.ui.home.HomeLayoutMode, Pair<Int, Int>>()

    init {
        loadFeed()
    }

    fun loadFeed(isExplicitRefresh: Boolean = false) {
        if (loadJob?.isActive == true) return
        
        loadJob = viewModelScope.launch {
            if (_uiState.value !is HomeUiState.Success) {
                // Try to load cached explicit current issue
                val knownIssue = currentIssueStore.lastKnownCurrentIssueNumber
                val snapshot = currentIssueStore.feedSnapshot
                var loadedFromCache = false
                
                if (knownIssue != null && snapshot != null && snapshot.issueNumber == knownIssue) {
                    val cachedSources = db.articleDao().getSourcesForIssue(knownIssue).associateBy { it.canonicalUrl }
                    if (cachedSources.isNotEmpty()) {
                        // Reconstruct feed faithfully from the presentation snapshot
                        val summaries = snapshot.articles.mapNotNull { snap ->
                            val entity = cachedSources[snap.canonicalUrl] ?: return@mapNotNull null
                            ArticleSummary(
                                url = snap.canonicalUrl,
                                title = entity.title,
                                author = entity.author ?: DEFAULT_ARTICLE_AUTHOR,
                                isPremium = snap.isPremium,
                                isFree = snap.isFree,
                                summary = entity.summary ?: "",
                                heroImageUrl = entity.heroImageUrl,
                                categoryLabel = entity.categoryLabel,
                                subcategoryLabel = entity.subcategoryLabel,
                                visualPlacement = snap.visualPlacement,
                                cardTreatment = pt.avante.app.data.CardTreatment.valueOf(snap.cardTreatment)
                            )
                        }
                        
                        if (summaries.isNotEmpty()) {
                            val feed = HomeFeed(editionText = snapshot.editionLabel, editionNumber = knownIssue, articles = summaries)
                            _uiState.value = HomeUiState.Success(feed, cached = true)
                            loadedFromCache = true
                        }
                    }
                }
                
                if (!loadedFromCache) {
                    _uiState.value = HomeUiState.Loading
                }
            }
            
            // Now fetch from network
            val fetchedFeedRaw = ArticleParser.fetchHomeFeed()
            
            if (fetchedFeedRaw != null) {
                // Resolve feed image ownership before any emission
                val resolvedArticles = pt.avante.app.ui.presentation.ArticleImageFrequencyAnalyzer.resolveFeedImages(fetchedFeedRaw.articles)
                val fetchedFeed = fetchedFeedRaw.copy(articles = resolvedArticles)
                
                currentIssueStore.lastKnownCurrentIssueNumber = fetchedFeed.editionNumber
                
                // Save the presentation snapshot
                val snapshots = resolvedArticles.map {
                    pt.avante.app.data.ArticlePresentationSnapshot(
                        canonicalUrl = it.url,
                        isPremium = it.isPremium,
                        isFree = it.isFree,
                        visualPlacement = it.visualPlacement,
                        cardTreatment = it.cardTreatment.name
                    )
                }
                currentIssueStore.feedSnapshot = pt.avante.app.data.FeedPresentationSnapshot(
                    issueNumber = fetchedFeed.editionNumber,
                    editionLabel = fetchedFeed.editionText,
                    articles = snapshots
                )

                saveSources(fetchedFeed)

                // Emitir já o feed (rápido): imagens/autores em falta são completados em background
                _uiState.value = HomeUiState.Success(fetchedFeed, cached = false)

                // Fase 2: completar imagens/autores sem bloquear a UI
                val enhanced = ArticleParser.enhanceFeedArticles(fetchedFeed.articles)
                if (enhanced != fetchedFeed.articles) {
                    val enhancedFeed = fetchedFeed.copy(articles = enhanced)
                    saveSources(enhancedFeed)
                    _uiState.value = HomeUiState.Success(enhancedFeed, cached = false)
                }
            } else {
                if (_uiState.value !is HomeUiState.Success) {
                    _uiState.value = HomeUiState.Error("Falha ao carregar e não há dados em cache.")
                }
                // If we already have data displayed, don't show an error over it.
            }
        }
    }

    private suspend fun saveSources(feed: HomeFeed) {
        val existingSources = db.articleDao().getAllSources().associateBy { it.articleId }
        val entities = feed.articles.map { summary ->
            val articleId = ArticleIdentity.generateStableId(summary.url)
            val existing = existingSources[articleId]
            ArticleEntity(
                articleId = articleId,
                canonicalUrl = summary.url,
                title = summary.title,
                author = summary.author,
                publication = Publication.AVANTE.displayName,
                issueNumber = feed.editionNumber,
                publishedAt = null,
                accessTier = if (summary.isPremium) "PREMIUM" else "FREE",
                summary = summary.summary,
                heroImageUrl = summary.heroImageUrl,
                categoryLabel = summary.categoryLabel ?: existing?.categoryLabel,
                subcategoryLabel = summary.subcategoryLabel ?: existing?.subcategoryLabel
            )
        }
        db.articleDao().insertSources(entities)
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val feed: HomeFeed, val cached: Boolean = false) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
