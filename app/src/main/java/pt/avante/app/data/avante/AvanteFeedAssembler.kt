package pt.avante.app.data.avante

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.DEFAULT_ARTICLE_AUTHOR
import pt.avante.app.data.HomeFeed
import pt.avante.app.data.Publication
import org.jsoup.nodes.Document
import pt.avante.app.data.auth.AvanteHttp

object AvanteFeedAssembler {

    suspend fun fetchHomeFeed(): HomeFeed? = withContext(Dispatchers.IO) {
        try {
            val doc = AvanteHttp.get("${AvanteSite.BASE_URL}/")

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
                
                val compatSection = semanticCategory ?: visualPlacement ?: Publication.AVANTE.displayName
                
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
                    author = indexMeta.author ?: DEFAULT_ARTICLE_AUTHOR,
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

            HomeFeed(
                editionText = editionText,
                editionNumber = editionNumber,
                articles = articles
            )
        } catch (e: Exception) {
            Log.w("AvanteFeedAssembler", "Falha ao montar o feed da edição", e)
            null
        }
    }

    /**
     * Fase 2 (background): completa imagens/autores em falta com fetches por artigo.
     * O feed é emitido primeiro sem isto, para a primeira abertura ser rápida.
     */
    suspend fun enhanceArticles(articles: List<ArticleSummary>): List<ArticleSummary> = withContext(Dispatchers.IO) {
        // O avante.pt serve os pedidos quase em série: com 10 em paralelo o artigo que o leitor
        // abre fica em fila (0,8 s → 3,9 s medido) e chega a passar o timeout de 15 s no telemóvel.
        val semaphore = Semaphore(2)
        coroutineScope {
            articles.map { summary ->
                async {
                    semaphore.withPermit {
                        var finalImage = summary.heroImageUrl
                        var finalAuthor = summary.author

                        val needsImage = finalImage == "LOCAL_SYMBOL" || finalImage == null
                        val needsAuthor = finalAuthor == DEFAULT_ARTICLE_AUTHOR

                        if (needsImage || needsAuthor) {
                            try {
                                val articleDoc = AvanteHttp.get(summary.url)
                                if (needsImage) {
                                    finalImage = ownArticleImage(articleDoc)
                                }
                                if (needsAuthor) {
                                    val extractedAuthor = articleDoc.select(".author, .autor").first()?.text()
                                    if (!extractedAuthor.isNullOrBlank()) {
                                        finalAuthor = extractedAuthor
                                    }
                                }
                            } catch (e: Exception) {
                                Log.w("AvanteFeedAssembler", "Falha ao completar ${summary.url}", e)
                            }
                        }
                        summary.copy(heroImageUrl = finalImage, author = finalAuthor)
                    }
                }
            }.awaitAll()
        }
    }

    /**
     * A imagem do próprio artigo: só dentro do primeiro `article` da página. Os seguintes são os
     * outros da secção, e a imagem de um deles passava a ser a de todas as Opiniões sem imagem.
     */
    internal fun ownArticleImage(doc: Document): String? =
        AvanteArticleParser.selectArticleContainer(doc).select("img")
            .map { it.attr("src") }
            .firstOrNull(AvanteImageRules::isArticleImage)
            ?.let { AvanteImageRules.normalizeImageUrl(it) }
}
