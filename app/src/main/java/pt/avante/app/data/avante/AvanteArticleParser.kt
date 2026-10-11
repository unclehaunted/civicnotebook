package pt.avante.app.data.avante

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.nodes.Element
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.Publication
import pt.avante.app.data.auth.AvanteHttp

object AvanteArticleParser {

    suspend fun fetchAvanteArticle(url: String, section: String? = null, heroImageFromFeed: String? = null): Article = withContext(Dispatchers.IO) {
        val doc = AvanteHttp.get(url)
        
        val title = doc.select("article h2.balance-text").first()?.text() 
            ?: doc.title().substringBefore("|").trim()
            
        val author = doc.select(".author, .autor").first()?.text() ?: "Redacção"
        val publishedAt = doc.select(".date, .data").first()?.text() ?: "Edição Actual"
        
        val articleContainer = doc.select("article").first() ?: doc.body()
        
        // Find valid image inside canonical article hero or body
        val articleImgCandidate = articleContainer.select(".hero-content img, .article-body img, .conteudo-artigo img").firstOrNull { img ->
            val src = img.attr("src")
            !AvanteImageRules.isPlaceholderImage(src)
        }?.attr("src")
        
        val articlePageImage = AvanteImageRules.normalizeImageUrl(articleImgCandidate)
        
        val validatedFeedImage = heroImageFromFeed
            ?.let(AvanteImageRules::normalizeImageUrl)
            ?.takeIf { it != "LOCAL_SYMBOL" && !AvanteImageRules.isPlaceholderImage(it) }

        val heroImageUrl = articlePageImage ?: validatedFeedImage
        
        val blocks = AvanteBodyParser.parseBodyContainer(articleContainer)
        
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
}
