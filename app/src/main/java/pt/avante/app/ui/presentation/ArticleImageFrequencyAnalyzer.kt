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
     * Identifies remote images that appear at least three times in the provided article list.
     * Returns a set of normalized URL identities.
     */
    fun findRepeatedImages(articles: List<ArticleSummary>): Set<String> {
        val frequencies = mutableMapOf<String, Int>()
        
        articles.forEach { article ->
            val normalized = normalizeUrl(article.heroImageUrl)
            if (normalized != null) {
                frequencies[normalized] = frequencies.getOrDefault(normalized, 0) + 1
            }
        }
        
        return frequencies.filter { it.value >= 3 }.keys
    }
}
