package pt.avante.app.data.db

import org.junit.Test
import org.junit.Assert.*
import pt.avante.app.data.Article
import pt.avante.app.data.Publication

class MappingTest {

    @Test
    fun testDomainMapping() {
        val entity = ArticleEntity(
            articleId = "123",
            canonicalUrl = "https://avante.pt",
            title = "O Partido",
            author = "Redacção",
            publication = "Avante!",
            issueNumber = "2745",
            publishedAt = "2026-07-16",
            accessTier = "FREE",
            summary = "Summary",
            heroImageUrl = null,
            categoryLabel = "Editorial",
            subcategoryLabel = "PCP"
        )
        
        val article = entity.toArticle(emptyList())
        
        assertEquals("Editorial", article.categoryLabel)
        assertEquals("PCP", article.subcategoryLabel)
        assertEquals("2745", article.issueNumber)
        assertEquals("Redacção", article.author)
        assertEquals(Publication.AVANTE, article.publication)
        assertEquals("https://avante.pt", article.canonicalUrl)
        assertEquals("2026-07-16", article.publishedAt)
    }

    @Test
    fun testPublicationMapping() {
        // Avante cases
        assertEquals(Publication.AVANTE, pt.avante.app.data.PublicationMapper.map("AVANTE"))
        assertEquals(Publication.AVANTE, pt.avante.app.data.PublicationMapper.map("Avante"))
        assertEquals(Publication.AVANTE, pt.avante.app.data.PublicationMapper.map("Avante!"))
        assertEquals(Publication.AVANTE, pt.avante.app.data.PublicationMapper.map("avante"))

        // OMILITANTE cases
        assertEquals(Publication.OMILITANTE, pt.avante.app.data.PublicationMapper.map("OMILITANTE"))
        assertEquals(Publication.OMILITANTE, pt.avante.app.data.PublicationMapper.map("O Militante"))
        assertEquals(Publication.OMILITANTE, pt.avante.app.data.PublicationMapper.map("o militante"))
        assertEquals(Publication.OMILITANTE, pt.avante.app.data.PublicationMapper.map("omilitante"))

        // Fallback / Unknown cases
        assertEquals(Publication.AVANTE, pt.avante.app.data.PublicationMapper.map(""))
        assertEquals(Publication.AVANTE, pt.avante.app.data.PublicationMapper.map("Unknown"))
        assertEquals(Publication.AVANTE, pt.avante.app.data.PublicationMapper.map(null))
    }
}
