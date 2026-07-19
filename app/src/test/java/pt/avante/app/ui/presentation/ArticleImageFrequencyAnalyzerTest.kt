package pt.avante.app.ui.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.CardTreatment

class ArticleImageFrequencyAnalyzerTest {

    private fun mockArticle(imageUrl: String?, articleUrl: String = "http://example.com/${java.util.UUID.randomUUID()}"): ArticleSummary {
        return ArticleSummary(
            url = articleUrl,
            title = "Test Article",
            summary = "Summary",
            heroImageUrl = imageUrl,
            categoryLabel = "Test",
            subcategoryLabel = null,
            author = "Redacção",
            cardTreatment = CardTreatment.STANDARD_SMALL
        )
    }

    @Test
    fun `One occurrence is retained`() {
        val article = mockArticle("https://example.com/img.jpg")
        val ownership = ArticleImageFrequencyAnalyzer.resolveFeedImageOwnership(listOf(article))
        assertEquals("https://example.com/img.jpg", ownership[article.url])
    }

    @Test
    fun `Two occurrences are suppressed`() {
        val article1 = mockArticle("https://example.com/img.jpg")
        val article2 = mockArticle("https://example.com/img.jpg")
        val ownership = ArticleImageFrequencyAnalyzer.resolveFeedImageOwnership(listOf(article1, article2))
        assertEquals(null, ownership[article1.url])
        assertEquals(null, ownership[article2.url])
    }

    @Test
    fun `Three occurrences are suppressed`() {
        val article1 = mockArticle("https://example.com/img.jpg")
        val article2 = mockArticle("https://example.com/img.jpg")
        val article3 = mockArticle("https://example.com/img.jpg")
        val ownership = ArticleImageFrequencyAnalyzer.resolveFeedImageOwnership(listOf(article1, article2, article3))
        assertEquals(null, ownership[article1.url])
    }

    @Test
    fun `Known placeholder is suppressed immediately`() {
        val article = mockArticle("https://example.com/simbolo_foice.png")
        val ownership = ArticleImageFrequencyAnalyzer.resolveFeedImageOwnership(listOf(article))
        assertEquals(null, ownership[article.url])
    }
}
