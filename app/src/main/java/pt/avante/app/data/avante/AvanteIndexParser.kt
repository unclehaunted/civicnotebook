package pt.avante.app.data.avante

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class IndexMetadata(
    val url: String,
    val title: String?,
    val author: String?,
    val categoryLabel: String?,
    val subcategoryLabel: String?,
    val isFree: Boolean,
    val indexImageUrl: String?,
    val intro: String?
)

object AvanteIndexParser {

    suspend fun fetchIndexInventory(editionNumber: String): Map<String, IndexMetadata> = withContext(Dispatchers.IO) {
        try {
            val indexUrl = "https://avante.pt/pt/$editionNumber/?tpl=183"
            val doc = Jsoup.connect(indexUrl).get()
            
            val inventory = mutableMapOf<String, IndexMetadata>()
            
            var currentCategory: String? = null
            var currentSubcategory: String? = null
            
            val contentArea = doc.select("#grid-indice, .content-indice, main, .main-content").firstOrNull() ?: doc.body()
            
            val elements = contentArea.select("div.seccao, h2, h3, h5, article, a[href\$='.htm']")
            
            for (element in elements) {
                when {
                    element.tagName() == "div" && element.hasClass("seccao") -> {
                        val canonical = extractCategoryFromSectionDiv(element)
                        if (canonical != null) {
                            currentCategory = canonical
                            currentSubcategory = null
                        }
                    }
                    
                    (element.tagName() == "h2" || element.tagName() == "h3") && 
                    element.parents().none { it.tagName() == "article" } -> {
                        val headerText = element.ownText().trim()
                        val hasArticleLink = element.select("a[href\$='.htm']").isNotEmpty()
                        
                        if (!hasArticleLink) {
                            val canonical = AvanteCategoryRules.canonicalCategoryLabel(headerText)
                            if (canonical != null) {
                                currentCategory = canonical
                                currentSubcategory = null
                            }
                        }
                    }
                    
                    element.tagName() == "h5" && 
                    element.parents().none { it.tagName() == "article" } &&
                    currentCategory != null -> {
                        val subcategoryText = element.text().trim()
                        if (subcategoryText.isNotBlank() && subcategoryText.length <= 60) {
                            currentSubcategory = subcategoryText
                        }
                    }
                    
                    element.tagName() == "article" -> {
                        extractArticleFromIndexElement(element, currentCategory, currentSubcategory, inventory)
                    }
                    
                    element.tagName() == "a" && element.attr("href").endsWith(".htm") &&
                    element.parents().none { it.tagName() == "article" } -> {
                        extractArticleFromLinkElement(element, currentCategory, currentSubcategory, inventory)
                    }
                }
            }
            
            inventory
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun extractCategoryFromSectionDiv(section: Element): String? {
        val directCategoryLinkText = section.children()
            .firstOrNull { child ->
                child.tagName().equals("a", ignoreCase = true) &&
                !child.attr("href").contains(".htm")
            }
            ?.text()
            ?.trim()
        
        val fromLink = AvanteCategoryRules.canonicalCategoryLabel(directCategoryLinkText)
        if (fromLink != null) return fromLink
        
        val fromDataAttr = AvanteCategoryRules.canonicalCategoryLabel(section.attr("data-section"))
        if (fromDataAttr != null) return fromDataAttr
        
        val sectionId = section.id().takeIf { it.isNotBlank() }
        return AvanteCategoryRules.canonicalCategoryLabel(sectionId)
    }

    internal fun extractArticleFromIndexElement(
        article: Element,
        currentCategory: String?,
        currentSubcategory: String?,
        inventory: MutableMap<String, IndexMetadata>
    ) {
        val link = article.select("a[href\$='.htm']").firstOrNull() ?: return
        val url = link.attr("href").trim()
        if (url.isBlank()) return
        
        val normalizedUrl = AvanteUrlUtils.normalizeUrl(url)
        
        if (inventory.containsKey(normalizedUrl)) return
        
        val title = link.text().trim().takeIf { it.isNotBlank() }
            ?: article.select("h2, h3").firstOrNull()?.text()?.trim()
        
        val intro = article.select(".texto-intro, p").firstOrNull()?.text()?.trim()
        
        val rawImageUrl = article.select("img").firstOrNull()?.attr("src")
        val indexImageUrl = if (rawImageUrl != null && !AvanteImageRules.isPlaceholderImage(rawImageUrl)) {
            AvanteImageRules.normalizeImageUrl(rawImageUrl)
        } else {
            null
        }
        
        val isFree = article.select("h4:contains(Artigo de acesso livre)").isNotEmpty()
        val authorText = article.select(".author, .autor").firstOrNull()?.text()?.trim()

        val articleSubcategory = article.select("h5")
            .firstOrNull()
            ?.text()
            ?.trim()
            ?.takeIf { it.isNotBlank() && it.length <= 60 }
            
        val resolvedSubcategory = articleSubcategory ?: currentSubcategory
        
        inventory[normalizedUrl] = IndexMetadata(
            url = normalizedUrl,
            title = title,
            author = authorText,
            categoryLabel = currentCategory,
            subcategoryLabel = resolvedSubcategory,
            isFree = isFree,
            indexImageUrl = indexImageUrl,
            intro = intro
        )
    }

    private fun extractArticleFromLinkElement(
        link: Element,
        currentCategory: String?,
        currentSubcategory: String?,
        inventory: MutableMap<String, IndexMetadata>
    ) {
        val url = link.attr("href").trim()
        if (url.isBlank()) return
        
        val normalizedUrl = AvanteUrlUtils.normalizeUrl(url)
        
        if (inventory.containsKey(normalizedUrl)) return
        
        val title = link.text().trim().takeIf { it.isNotBlank() }
        val intro = link.parent()?.select("p")?.firstOrNull()?.text()?.trim()
        val isFree = link.parent()?.select("h4:contains(Artigo de acesso livre)")?.isNotEmpty() ?: false
        val authorText = link.parent()?.select(".author, .autor")?.firstOrNull()?.text()?.trim()
        
        inventory[normalizedUrl] = IndexMetadata(
            url = normalizedUrl,
            title = title,
            author = authorText,
            categoryLabel = currentCategory,
            subcategoryLabel = currentSubcategory,
            isFree = isFree,
            indexImageUrl = null,
            intro = intro
        )
    }
}
