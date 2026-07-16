package pt.avante.app.data.avante

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.Publication

object AvanteArticleParser {

    suspend fun fetchAvanteArticle(url: String, section: String? = null, heroImageFromFeed: String? = null): Article = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url).get()
        
        val title = doc.select("article h2.balance-text").first()?.text() 
            ?: doc.title().substringBefore("|").trim()
            
        val author = doc.select(".author, .autor").first()?.text() ?: "Redacção"
        val publishedAt = doc.select(".date, .data").first()?.text() ?: "Edição Actual"
        
        var heroImageUrl: String? = null
        
        if (heroImageFromFeed != null && heroImageFromFeed != "LOCAL_SYMBOL") {
            heroImageUrl = heroImageFromFeed
        } else {
            var articleImg = doc.select("article img").first()?.attr("src")
            if (articleImg == null) {
                articleImg = doc.select("section img, .content img").first()?.attr("src")
            }
            heroImageUrl = AvanteImageRules.normalizeImageUrl(articleImg)
        }
        
        val blocks = mutableListOf<ArticleBlock>()
        
        val paragraphs = doc.select("article p, article div.texto-intro")
            .filter { p -> 
                p.parents().none { it.tagName() in listOf("figure", "figcaption") }
            }
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
        
        val onlyStrong =
            p.children().size == 1 &&
            p.children().firstOrNull()?.tagName()?.equals("strong", ignoreCase = true) == true &&
            p.ownText().trim().isBlank() &&
            text.length <= 100
            
        if (onlyStrong) {
            blocks.add(ArticleBlock.Subheading(text))
            return blocks
        }
        
        blocks.add(ArticleBlock.Text(text))
        return blocks
    }
}
