package pt.avante.app.data.avante

import org.jsoup.nodes.Document

data class HomepagePlacementData(
    val visualPlacement: String,
    val homepageOrder: Int,
    val homepageImageCandidate: String?
)

object AvanteHomepageOverlayParser {
    
    fun extractHomepagePlacement(doc: Document): Map<String, HomepagePlacementData> {
        val homepagePlacement = mutableMapOf<String, HomepagePlacementData>()
        val sections = doc.select(".content-front section")
        var homepageOrder = 0
        
        for (section in sections) {
            var currentCategory = "Destaque"
            
            val sectionHeader = section.selectFirst("h2, h3")
            if (sectionHeader != null && sectionHeader.parents().none { it.tagName() == "article" }) {
                val headerText = sectionHeader.text().trim()
                if (headerText.isNotEmpty()) {
                    currentCategory = headerText
                }
            }
            
            val sectionArticles = section.select("article")
            for (el in sectionArticles) {
                val titleNode = el.select("h2 a, h3 a").first() ?: continue
                val url = titleNode.attr("href").trim()
                if (url.isEmpty()) continue
                
                val fullUrl = AvanteUrlUtils.normalizeUrl(url)
                
                val rawImageUrl = el.select("figure img, .noticia-foto img, img").first()?.attr("src")
                val homepageImageCandidate = when {
                    rawImageUrl == null || rawImageUrl.isBlank() -> null
                    AvanteImageRules.isPlaceholderImage(rawImageUrl) -> "LOCAL_SYMBOL"
                    else -> AvanteImageRules.normalizeImageUrl(rawImageUrl)
                }
                
                homepagePlacement[fullUrl] = HomepagePlacementData(
                    visualPlacement = currentCategory,
                    homepageOrder = homepageOrder++,
                    homepageImageCandidate = homepageImageCandidate
                )
            }
        }
        
        return homepagePlacement
    }
}
