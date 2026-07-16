package pt.avante.app.data

import kotlinx.serialization.Serializable

enum class Publication {
    AVANTE, OMILITANTE
}

data class Article(
    val id: String,
    val title: String,
    val author: String,
    val publishedAt: String,
    val canonicalUrl: String,
    val heroImageUrl: String? = null,
    val publication: Publication,
    val section: String? = null,
    val summary: String = "",
    val blocks: List<ArticleBlock> = emptyList()
)

@Serializable
sealed class ArticleBlock {
    @Serializable
    data class Text(val content: String) : ArticleBlock()
    @Serializable
    data class Image(val url: String, val caption: String) : ArticleBlock()
    @Serializable
    data class Subheading(val text: String) : ArticleBlock()
    @Serializable
    data object PaywallCTA : ArticleBlock()
}

data class ArticleSummary(
    val title: String,
    val author: String,
    val url: String,
    val isPremium: Boolean = false,
    val heroImageUrl: String? = null,
    val summary: String,
    val section: String? = null,
    val isFree: Boolean = true // Explicitly marked as free access in index
)

data class HomeFeed(
    val editionText: String,
    val editionNumber: String,
    val articles: List<ArticleSummary>
)
