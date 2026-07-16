package pt.avante.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.nodes.Element

object ArticleParser {

    suspend fun fetchAvanteArticle(url: String, section: String? = null): Article = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url).get()
        
        // Avante! specific structure
        val title = doc.select("article h2.balance-text").first()?.text() 
            ?: doc.title().substringBefore("|").trim()
            
        val author = doc.select(".author, .autor").first()?.text() ?: "Redacção"
        val publishedAt = doc.select(".date, .data").first()?.text() ?: "Edição Actual"
        
        // Extract the main hero image
        var heroImageUrl = doc.select("article img").first()?.attr("src")
        if (heroImageUrl != null) {
            if (heroImageUrl.contains("simbolo_foice_fundo_branco.png") || heroImageUrl.contains("ImageId=41")) {
                heroImageUrl = "LOCAL_SYMBOL"
            } else if (heroImageUrl.startsWith("/")) {
                heroImageUrl = "https://avante.pt$heroImageUrl"
            }
        }
        
        val blocks = mutableListOf<ArticleBlock>()
        
        // Extract paragraphs inside the article tag
        val paragraphs = doc.select("article p, article div.texto-intro")
        for (p in paragraphs) {
            val text = p.text().trim()
            if (text.contains("O artigo completo está disponível") || text.contains("Compra o «Avante!»")) {
                blocks.add(ArticleBlock.PaywallCTA)
                break
            }
            if (text.isNotBlank() && !text.contains("Já é assinante ou comprou")) {
                blocks.addAll(parseParagraphElement(p))
            }
        }
        
        Article(
            id = url.hashCode().toString(),
            title = title,
            author = author,
            publishedAt = publishedAt,
            canonicalUrl = url,
            heroImageUrl = heroImageUrl,
            publication = Publication.AVANTE,
            section = section,
            blocks = blocks
        )
    }

    private fun parseParagraphElement(p: Element): List<ArticleBlock> {
        val blocks = mutableListOf<ArticleBlock>()
        
        val text = p.text().trim()
        if (text.isBlank()) return emptyList()
        
        // Only treat as subheading if there's EXACTLY one <strong> child,
        // no text outside it, and the text is reasonably short (subheadings are brief)
        val onlyStrong =
            p.children().size == 1 &&
            p.children().firstOrNull()?.tagName()?.equals("strong", ignoreCase = true) == true &&
            p.ownText().trim().isBlank() &&
            text.length <= 100
            
        if (onlyStrong) {
            blocks.add(ArticleBlock.Subheading(text))
            return blocks
        }
        
        // For all other cases, treat as regular text to avoid duplication
        blocks.add(ArticleBlock.Text(text))
        return blocks
    }

    suspend fun fetchHomeFeed(): HomeFeed? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("https://avante.pt/").get()

            // 1. Fetch Edition Name and Number
            val editionNode = doc.select("#simbolo").first()
            val editionText = editionNode?.text()?.trim() ?: "Nº Desconhecido"
            val editionNumber = Regex("""n\.º\s*(\d+)""").find(editionText)?.groupValues?.get(1) ?: ""
            
            val articles = mutableListOf<ArticleSummary>()
            
            // 2. Sequentially parse the homepage content to track categories
            val elements = doc.select(".content-front h2, .content-front h3, .content-front article")
            var currentCategory = "Destaque"
            
            for (el in elements) {
                // Update Category if we hit a header that doesn't belong to an article
                if (el.tagName() in listOf("h2", "h3")) {
                    // Check if it's not nested inside an article
                    if (el.parents().none { it.tagName() == "article" }) {
                        val headerText = el.text().trim()
                        if (headerText.isNotEmpty()) {
                            currentCategory = headerText
                        }
                    }
                } 
                // Parse Article Box
                else if (el.tagName() == "article") {
                    val titleNode = el.select("h2 a").first() ?: continue
                    val title = titleNode.text().trim()
                    val url = titleNode.attr("href").trim()
                    
                    if (title.isEmpty() || url.isEmpty()) continue
                    
                    val fullUrl = if (url.startsWith("http")) url else "https://avante.pt$url"
                    // Author isn't usually on the homepage cards, it's inside the article. Fallback to Redacção
                    val author = "Redacção" 
                    val summary = el.select(".texto-intro p").firstOrNull()?.text()?.trim() ?: ""
                    
                    var heroImageUrl = el.select("figure img, .noticia-foto img, img").first()?.attr("src")
                    if (heroImageUrl != null) {
                        if (heroImageUrl.contains("simbolo_foice_fundo_branco.png") || heroImageUrl.contains("ImageId=41")) {
                            heroImageUrl = "LOCAL_SYMBOL"
                        } else if (!heroImageUrl.startsWith("http")) {
                            heroImageUrl = "https://avante.pt$heroImageUrl"
                        }
                    }
                    
                    val isPremium = !el.text().lowercase().contains("livre")
                    
                    articles.add(ArticleSummary(
                        title = title,
                        author = author,
                        url = fullUrl,
                        heroImageUrl = heroImageUrl,
                        summary = summary,
                        isPremium = isPremium,
                        section = currentCategory
                    ))
                }
            }

            // Concurrently fetch true images for any LOCAL_SYMBOL fallbacks
            val resolvedArticles = coroutineScope {
                articles.map { summary ->
                    async {
                        var finalImage = summary.heroImageUrl
                        if (finalImage == "LOCAL_SYMBOL" || finalImage == null) {
                            try {
                                val articleDoc = Jsoup.connect(summary.url).get()
                                val articleImg = articleDoc.select("article img").first()?.attr("src")
                                if (articleImg != null && !articleImg.contains("simbolo_foice") && !articleImg.contains("ImageId=41")) {
                                    finalImage = if (articleImg.startsWith("/")) "https://avante.pt$articleImg" else articleImg
                                }
                            } catch (e: Exception) {
                                // Keep LOCAL_SYMBOL on error
                            }
                        }
                        summary.copy(heroImageUrl = finalImage)
                    }
                }.awaitAll()
            }

            HomeFeed(
                editionText = editionText,
                editionNumber = editionNumber,
                articles = resolvedArticles
            )
        } catch (e: Exception) {
            null
        }
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
                "PAYWALL_CTA" -> ArticleBlock.PaywallCTA
                else -> null
            }
        }
    }
}
