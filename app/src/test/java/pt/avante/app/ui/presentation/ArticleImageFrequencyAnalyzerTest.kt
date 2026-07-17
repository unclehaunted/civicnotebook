package pt.avante.app.ui.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.CardTreatment

class ArticleImageFrequencyAnalyzerTest {

    private fun mockArticle(url: String?): ArticleSummary {
        return ArticleSummary(
            url = "http://example.com/1",
            title = "Test Article",
            summary = "Summary",
            heroImageUrl = url,
            categoryLabel = "Test",
            subcategoryLabel = null,
            author = "Redacção",
            cardTreatment = CardTreatment.STANDARD_SMALL
        )
    }

    @Test
    fun `Two occurrences are retained`() {
        val articles = listOf(
            mockArticle("https://example.com/img.jpg"),
            mockArticle("https://example.com/img.jpg")
        )
        val repeated = ArticleImageFrequencyAnalyzer.findRepeatedImages(articles)
        assertEquals(emptySet<String>(), repeated)
    }

    @Test
    fun `Three occurrences are suppressed`() {
        val articles = listOf(
            mockArticle("https://example.com/img.jpg"),
            mockArticle("https://example.com/img.jpg"),
            mockArticle("https://example.com/img.jpg")
        )
        val repeated = ArticleImageFrequencyAnalyzer.findRepeatedImages(articles)
        assertEquals(setOf("example.com/img.jpg"), repeated)
    }

    @Test
    fun `LOCAL_SYMBOL and blanks are ignored`() {
        val articles = listOf(
            mockArticle("LOCAL_SYMBOL"),
            mockArticle("LOCAL_SYMBOL"),
            mockArticle("LOCAL_SYMBOL"),
            mockArticle("   "),
            mockArticle(""),
            mockArticle(null)
        )
        val repeated = ArticleImageFrequencyAnalyzer.findRepeatedImages(articles)
        assertEquals(emptySet<String>(), repeated)
    }

    @Test
    fun `Equivalent normalized URLs count together`() {
        val articles = listOf(
            mockArticle("http://example.com/img.jpg"),
            mockArticle("https://www.example.com/img.jpg"),
            mockArticle("  example.com/img.jpg  ")
        )
        val repeated = ArticleImageFrequencyAnalyzer.findRepeatedImages(articles)
        assertEquals(setOf("example.com/img.jpg"), repeated)
    }
}
