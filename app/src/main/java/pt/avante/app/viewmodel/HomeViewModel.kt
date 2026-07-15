package pt.avante.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pt.avante.app.data.ArticleParser
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.HomeFeed
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.ArticleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest

class HomeViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            
            val fetchedFeed = ArticleParser.fetchHomeFeed()
            
            if (fetchedFeed != null) {
                val entities = fetchedFeed.articles.mapIndexed { index, summary ->
                    val articleId = generateStableId(summary.url)
                    ArticleEntity(
                        articleId = articleId,
                        canonicalUrl = summary.url,
                        title = summary.title,
                        author = summary.author,
                        publication = "Avante!",
                        issueNumber = fetchedFeed.editionNumber,
                        publishedAt = fetchedFeed.editionNumber,
                        accessTier = if (summary.isPremium) "PREMIUM" else "FREE",
                        summary = summary.summary,
                        heroImageUrl = summary.heroImageUrl
                    )
                }
                
                db.articleDao().insertSources(entities)
                
                _uiState.value = HomeUiState.Success(fetchedFeed)
            } else {
                val cachedSources = db.articleDao().getAllSources()
                if (cachedSources.isNotEmpty()) {
                    val summaries = cachedSources.map { source ->
                        ArticleSummary(
                            title = source.title,
                            author = source.author ?: "Redacção",
                            url = source.canonicalUrl,
                            isPremium = source.accessTier == "PREMIUM",
                            heroImageUrl = source.heroImageUrl,
                            summary = source.summary ?: "",
                            section = "Arquivo"
                        )
                    }
                    val feed = HomeFeed(
                        editionText = "Edição em Cache",
                        editionNumber = cachedSources.firstOrNull()?.issueNumber ?: "",
                        articles = summaries
                    )
                    _uiState.value = HomeUiState.Success(feed, cached = true)
                } else {
                    _uiState.value = HomeUiState.Error("Falha ao carregar e não há dados em cache.")
                }
            }
        }
    }

    private fun generateStableId(url: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val feed: HomeFeed, val cached: Boolean = false) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
