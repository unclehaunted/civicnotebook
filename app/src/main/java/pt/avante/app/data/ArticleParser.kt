package pt.avante.app.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pt.avante.app.data.avante.AvanteArticleParser
import pt.avante.app.data.avante.AvanteFeedAssembler

object ArticleParser {

    suspend fun fetchAvanteArticle(url: String, section: String? = null, heroImageFromFeed: String? = null): Article {
        return AvanteArticleParser.fetchAvanteArticle(url, section, heroImageFromFeed)
    }

    suspend fun fetchHomeFeed(): HomeFeed? {
        return AvanteFeedAssembler.fetchHomeFeed()
    }

    suspend fun enhanceFeedArticles(articles: List<ArticleSummary>): List<ArticleSummary> {
        return AvanteFeedAssembler.enhanceArticles(articles)
    }
}

object ArticleBlockSerializer {

    private val format = Json { 
        ignoreUnknownKeys = true
        classDiscriminator = "type"
    }

    fun serialize(blocks: List<ArticleBlock>): String {
        return format.encodeToString(blocks)
    }

    fun deserialize(jsonString: String): List<ArticleBlock> {
        return try {
            if (jsonString.isBlank()) return emptyList()
            if (jsonString.contains("|||") && !jsonString.trim().startsWith("[")) {
                parseLegacy(jsonString)
            } else {
                format.decodeFromString(jsonString)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseLegacy(jsonString: String): List<ArticleBlock> {
        if (jsonString.isBlank()) return emptyList()
        return jsonString.split("|||").mapNotNull { blockStr ->
            val parts = blockStr.split("||")
            when (parts[0]) {
                "TEXT" -> ArticleBlock.Text(content = parts.getOrNull(1) ?: "")
                "IMAGE" -> ArticleBlock.Image(
                    url = parts.getOrNull(1) ?: "",
                    caption = parts.getOrNull(2) ?: ""
                )
                "SUBHEADING" -> ArticleBlock.Subheading(text = parts.getOrNull(1) ?: "")
                "QUOTE" -> ArticleBlock.Quote(content = parts.getOrNull(1) ?: "")
                "PAYWALL_CTA" -> ArticleBlock.PaywallCTA
                else -> null
            }
        }
    }
}
