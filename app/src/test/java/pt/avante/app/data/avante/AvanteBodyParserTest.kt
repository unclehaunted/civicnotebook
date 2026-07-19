package pt.avante.app.data.avante

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pt.avante.app.data.ArticleBlock

class AvanteBodyParserTest {

    @Test
    fun parseIntroWithParagraphs_avoidsDuplication() {
        val html = """
            <article>
                <div class="texto-intro">
                    <p>This is the intro text.</p>
                </div>
                <div class="texto-body">
                    <p>This is the body text.</p>
                </div>
            </article>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val container = doc.select("article").first()!!
        
        val blocks = AvanteBodyParser.parseBodyContainer(container)
        
        assertEquals(2, blocks.size)
        assertEquals("This is the intro text.", (blocks[0] as ArticleBlock.Text).content)
        assertEquals("This is the body text.", (blocks[1] as ArticleBlock.Text).content)
    }

    @Test
    fun parseDirectParagraphs() {
        val html = """
            <article>
                <p>First paragraph.</p>
                <p>Second paragraph.</p>
            </article>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val container = doc.select("article").first()!!
        
        val blocks = AvanteBodyParser.parseBodyContainer(container)
        
        assertEquals(2, blocks.size)
        assertEquals("First paragraph.", (blocks[0] as ArticleBlock.Text).content)
        assertEquals("Second paragraph.", (blocks[1] as ArticleBlock.Text).content)
    }

    @Test
    fun parseBlockquote() {
        val html = """
            <article>
                <p>Some text</p>
                <blockquote>This is a quote</blockquote>
            </article>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val container = doc.select("article").first()!!
        
        val blocks = AvanteBodyParser.parseBodyContainer(container)
        
        assertEquals(2, blocks.size)
        assertTrue(blocks[1] is ArticleBlock.Quote)
        assertEquals("This is a quote", (blocks[1] as ArticleBlock.Quote).content)
    }

    @Test
    fun parseSubheading() {
        val html = """
            <article>
                <p><strong>A subheading</strong></p>
                <h3>Another subheading</h3>
                <p>Normal text</p>
            </article>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val container = doc.select("article").first()!!
        
        val blocks = AvanteBodyParser.parseBodyContainer(container)
        
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is ArticleBlock.Subheading)
        assertEquals("A subheading", (blocks[0] as ArticleBlock.Subheading).text)
        assertTrue(blocks[1] is ArticleBlock.Subheading)
        assertEquals("Another subheading", (blocks[1] as ArticleBlock.Subheading).text)
    }

    @Test
    fun parsePaywallCTA() {
        val html = """
            <article>
                <p>Free text</p>
                <p>O artigo completo está disponível para assinantes</p>
                <p>Should be ignored</p>
            </article>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val container = doc.select("article").first()!!
        
        val blocks = AvanteBodyParser.parseBodyContainer(container)
        
        assertEquals(2, blocks.size)
        assertTrue(blocks[1] is ArticleBlock.PaywallCTA)
    }
}
