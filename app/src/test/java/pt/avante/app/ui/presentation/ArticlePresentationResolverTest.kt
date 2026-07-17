package pt.avante.app.ui.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.CardTreatment

class ArticlePresentationResolverTest {

    private fun mockArticle(category: String, subcategory: String? = null, hasImage: Boolean = true): ArticleSummary {
        return ArticleSummary(
            url = "http://example.com/1",
            title = "Test Article",
            summary = "Summary",
            heroImageUrl = if (hasImage) "http://example.com/img.jpg" else null,
            categoryLabel = category,
            subcategoryLabel = subcategory,
            author = "Redacção",
            cardTreatment = CardTreatment.STANDARD_SMALL
        )
    }

    @Test
    fun `Opinião uses local placeholder in list and grid`() {
        val article = mockArticle("Opinião", "A talhe de foice", hasImage = false)

        val listPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.LIST)
        assertEquals(ArticleImagePolicy.OPINION_AH_PLACEHOLDER, listPresentation.imagePolicy)

        val gridPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.GRID)
        assertEquals(ArticleImagePolicy.OPINION_AH_PLACEHOLDER, gridPresentation.imagePolicy)
        assertEquals(ArticlePresentationVariant.OPINION_MEDIUM, gridPresentation.variant)
    }

    @Test
    fun `List and grid variants resolve correctly for lead`() {
        val article = mockArticle("Em Foco")
        
        val listPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.LIST)
        assertEquals(ArticlePresentationVariant.LEAD_LARGE, listPresentation.variant)

        val gridPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.GRID)
        assertEquals(ArticlePresentationVariant.LEAD_LARGE, gridPresentation.variant)
    }

    @Test
    fun `Ordinary grid articles resolve to GRID_TEXT_TILE without image`() {
        val article = mockArticle("Nacional", hasImage = false)

        val gridPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.GRID)
        assertEquals(ArticlePresentationVariant.GRID_TEXT_TILE, gridPresentation.variant)
        assertEquals(ArticleImagePolicy.NO_IMAGE, gridPresentation.imagePolicy)
    }

    @Test
    fun `Editorial never resolves smaller than lead`() {
        val articleNoImage = mockArticle("Editorial", hasImage = false)
        val listNoImage = ArticlePresentationResolver.resolvePresentation(articleNoImage, FeedViewMode.LIST)
        val gridNoImage = ArticlePresentationResolver.resolvePresentation(articleNoImage, FeedViewMode.GRID)
        assertEquals(ArticlePresentationVariant.LEAD_LARGE, listNoImage.variant)
        assertEquals(ArticlePresentationVariant.LEAD_LARGE, gridNoImage.variant)
    }

    @Test
    fun `Internacional never resolves smaller than medium`() {
        val articleNoImage = mockArticle("Internacional", hasImage = false)
        val listNoImage = ArticlePresentationResolver.resolvePresentation(articleNoImage, FeedViewMode.LIST)
        val gridNoImage = ArticlePresentationResolver.resolvePresentation(articleNoImage, FeedViewMode.GRID)
        assertEquals(ArticlePresentationVariant.FEATURED_MEDIUM, listNoImage.variant)
        assertEquals(ArticlePresentationVariant.FEATURED_MEDIUM, gridNoImage.variant)
    }

    @Test
    fun `Opinião and Argumentos never resolve to grid text tiles`() {
        val opiniao = mockArticle("Opinião", hasImage = false)
        val argumentos = mockArticle("Argumentos", hasImage = false)
        
        val gridOpiniao = ArticlePresentationResolver.resolvePresentation(opiniao, FeedViewMode.GRID)
        val gridArgumentos = ArticlePresentationResolver.resolvePresentation(argumentos, FeedViewMode.GRID)
        
        assertNotEquals(ArticlePresentationVariant.GRID_TEXT_TILE, gridOpiniao.variant)
        assertNotEquals(ArticlePresentationVariant.GRID_TEXT_TILE, gridArgumentos.variant)
    }


    @Test
    fun `Repeated image converts to NO_IMAGE in Medium card`() {
        val article = mockArticle("Internacional")
        val repeatedImages = setOf("example.com/img.jpg")
        
        val gridPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.GRID, repeatedImages)
        
        assertEquals(ArticlePresentationVariant.FEATURED_MEDIUM, gridPresentation.variant)
        assertEquals(ArticleImagePolicy.NO_IMAGE, gridPresentation.imagePolicy)
    }

    @Test
    fun `Non-repeated image stays REMOTE_IMAGE`() {
        val article = mockArticle("Internacional")
        val repeatedImages = setOf("http://example.com/other.jpg")
        
        val gridPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.GRID, repeatedImages)
        
        assertEquals(ArticlePresentationVariant.FEATURED_MEDIUM, gridPresentation.variant)
        assertEquals(ArticleImagePolicy.REMOTE_IMAGE, gridPresentation.imagePolicy)
    }
}
