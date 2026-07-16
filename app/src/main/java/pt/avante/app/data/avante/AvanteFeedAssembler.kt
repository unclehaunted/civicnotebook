package pt.avante.app.data.avante

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.HomeFeed

object AvanteFeedAssembler {

    suspend fun fetchHomeFeed(): HomeFeed? = withContext(Dispatchers.IO) {
        try {
            val doc = Jsoup.connect("https://avante.pt/").get()

            val editionNode = doc.select("#simbolo").first()
            val editionText = editionNode?.text()?.trim() ?: "Nº Desconhecido"
            val editionNumber = Regex("""n\.º\s*(\d+)""").find(editionText)?.groupValues?.get(1) ?: ""
            
            val indexInventory = AvanteIndexParser.fetchIndexInventory(editionNumber)
            
            val homepagePlacement = AvanteHomepageOverlayParser.extractHomepagePlacement(doc)
            
            val articles = indexInventory.map { (url, indexMeta) ->
                val placement = homepagePlacement[url]
                
                val visualPlacement = placement?.visualPlacement
                val heroImageUrl = placement?.homepageImageCandidate 
                    ?: indexMeta.indexImageUrl
                
                val semanticCategory = indexMeta.categoryLabel 
                    ?: AvanteCategoryRules.canonicalCategoryLabel(visualPlacement)
                val semanticSubcategory = indexMeta.subcategoryLabel
                
                val compatSection = semanticCategory ?: visualPlacement ?: "Avante!"
                
                val isFree = indexMeta.isFree
                val isPremium = !isFree
                
                val hasUsefulImage = heroImageUrl != null && heroImageUrl != "LOCAL_SYMBOL"
                val homepageOrder = placement?.homepageOrder ?: Int.MAX_VALUE

                val cardTreatment = CardTreatmentResolver.resolveCardTreatment(
                    categoryLabel = semanticCategory,
                    subcategoryLabel = semanticSubcategory,
                    visualPlacement = visualPlacement,
                    homepageOrder = homepageOrder,
                    hasUsefulImage = hasUsefulImage
                )
                
                ArticleSummary(
                    title = indexMeta.title ?: "Sem Título",
                    author = indexMeta.author ?: "Redacção",
                    url = url,
                    heroImageUrl = heroImageUrl,
                    summary = indexMeta.intro ?: "",
                    isPremium = isPremium,
                    section = compatSection,
                    isFree = isFree,
                    categoryLabel = semanticCategory,
                    subcategoryLabel = semanticSubcategory,
                    visualPlacement = visualPlacement,
                    cardTreatment = cardTreatment
                )
            }.sortedWith(compareBy(
                { homepagePlacement[it.url]?.homepageOrder ?: Int.MAX_VALUE },
                { it.categoryLabel ?: "zzz" },
                { it.title }
            ))

            val semaphore = Semaphore(10)
            val resolvedArticles = coroutineScope {
                articles.map { summary ->
                    async {
                        semaphore.withPermit {
                            var finalImage = summary.heroImageUrl
                            var finalAuthor = summary.author
                            
                            val needsImage = finalImage == "LOCAL_SYMBOL" || finalImage == null
                            val needsAuthor = finalAuthor == "Redacção"
                            
                            if (needsImage || needsAuthor) {
                                try {
                                    val articleDoc = Jsoup.connect(summary.url).get()
                                    if (needsImage) {
                                        var articleImg = articleDoc.select("article img").first()?.attr("src")
                                        if (articleImg == null) {
                                            articleImg = articleDoc.select("section img, .content img").first()?.attr("src")
                                        }
                                        finalImage = AvanteImageRules.normalizeImageUrl(articleImg)
                                    }
                                    if (needsAuthor) {
                                        val extractedAuthor = articleDoc.select(".author, .autor").first()?.text()
                                        if (!extractedAuthor.isNullOrBlank()) {
                                            finalAuthor = extractedAuthor
                                        }
                                    }
                                } catch (e: Exception) {
                                }
                            }
                            summary.copy(heroImageUrl = finalImage, author = finalAuthor)
                        }
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
