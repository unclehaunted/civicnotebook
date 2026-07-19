package pt.avante.app.ui.presentation

import pt.avante.app.data.ArticleSummary

object ArticleImageFrequencyAnalyzer {

    /**
     * Normalizes a URL to ensure equivalent absolute URLs are grouped together.
     * Trims whitespace and removes scheme (http/https) and optional 'www.' prefix.
     */
    fun normalizeUrl(url: String?): String? {
        if (url.isNullOrBlank() || url == "LOCAL_SYMBOL") return null
        return url.trim()
            .removePrefix("http://")
            .removePrefix("https://")
            .removePrefix("www.")
    }

    /**
     * Resolves feed-image ownership by rejecting ambiguous images.
     * If an image is assigned to more than one distinct canonical article URL, it is ambiguous and returns null.
     */
    fun resolveFeedImages(articles: List<ArticleSummary>): List<ArticleSummary> {
        val ownershipMap = resolveFeedImageOwnership(articles)
        return articles.map { it.copy(heroImageUrl = ownershipMap[it.url]) }
    }

    /**
     * Returns a map of canonical article URL to its validated feed image URL (or null if rejected).
     */
    fun resolveFeedImageOwnership(articles: List<ArticleSummary>): Map<String, String?> {
        val urlToArticles = mutableMapOf<String, MutableSet<String>>()
        
        articles.forEach { article ->
            val normalizedImg = normalizeUrl(article.heroImageUrl)
            if (normalizedImg != null) {
                urlToArticles.getOrPut(normalizedImg) { mutableSetOf() }.add(article.url)
            }
        }
        
        // Reject if it's assigned to more than 1 distinct canonical article URL
        val suspiciousNormalizedUrls = urlToArticles.filter { it.value.size > 1 }.keys
        
        val ownershipMap = mutableMapOf<String, String?>()
        articles.forEach { article ->
            val imgUrl = article.heroImageUrl
            val normalizedImg = normalizeUrl(imgUrl)
            
            if (normalizedImg == null || pt.avante.app.data.avante.AvanteImageRules.isPlaceholderImage(imgUrl)) {
                ownershipMap[article.url] = null
            } else if (suspiciousNormalizedUrls.contains(normalizedImg)) {
                ownershipMap[article.url] = null
            } else {
                ownershipMap[article.url] = imgUrl
            }
        }
        
        return ownershipMap
    }
}
