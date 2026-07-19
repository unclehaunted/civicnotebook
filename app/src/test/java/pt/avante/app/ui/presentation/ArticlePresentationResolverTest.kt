package pt.avante.app.ui.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.CardTreatment

class ArticlePresentationResolverTest {

    private fun mockArticle(cardTreatment: CardTreatment, hasImage: Boolean = true): ArticleSummary {
        return ArticleSummary(
            url = "http://example.com/1",
            title = "Test Article",
            summary = "Summary",
            heroImageUrl = if (hasImage) "http://example.com/img.jpg" else null,
            categoryLabel = "Test",
            subcategoryLabel = null,
            author = "Redacção",
            cardTreatment = cardTreatment
        )
    }

    @Test
    fun `Opinion compact uses local placeholder in list mode`() {
        val article = mockArticle(CardTreatment.OPINION_COMPACT, hasImage = false)

        val listPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.LIST)
        assertEquals(ArticleImagePolicy.OPINION_AH_PLACEHOLDER, listPresentation.imagePolicy)
        assertEquals(ArticlePresentationVariant.OPINION_MEDIUM, listPresentation.variant)

        val gridPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.GRID)
        // In grid mode opinion is just a text tile
        assertEquals(ArticleImagePolicy.NO_IMAGE, gridPresentation.imagePolicy)
        assertEquals(ArticlePresentationVariant.GRID_TEXT_TILE, gridPresentation.variant)
    }

    @Test
    fun `List and grid variants resolve correctly for lead large`() {
        val article = mockArticle(CardTreatment.LEAD_LARGE)
        
        val listPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.LIST)
        assertEquals(ArticlePresentationVariant.LEAD_LARGE, listPresentation.variant)

        val gridPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.GRID)
        assertEquals(ArticlePresentationVariant.LEAD_LARGE, gridPresentation.variant)
    }

    @Test
    fun `List and grid variants resolve correctly for featured medium`() {
        val article = mockArticle(CardTreatment.FEATURED_MEDIUM, hasImage = false)
        
        val listPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.LIST)
        assertEquals(ArticlePresentationVariant.FEATURED_MEDIUM, listPresentation.variant)

        val gridPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.GRID)
        assertEquals(ArticlePresentationVariant.GRID_TEXT_TILE, gridPresentation.variant)
    }

    @Test
    fun `Standard small articles resolve to GRID_TEXT_TILE without image`() {
        val article = mockArticle(CardTreatment.STANDARD_SMALL, hasImage = false)

        val gridPresentation = ArticlePresentationResolver.resolvePresentation(article, FeedViewMode.GRID)
        assertEquals(ArticlePresentationVariant.GRID_TEXT_TILE, gridPresentation.variant)
        assertEquals(ArticleImagePolicy.NO_IMAGE, gridPresentation.imagePolicy)
    }
}
