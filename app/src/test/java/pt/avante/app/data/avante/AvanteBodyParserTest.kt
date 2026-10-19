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

    // Os testes onlyStrong_* fixam o comportamento atual da heurística, não o ideal.

    private fun parse(body: String): List<ArticleBlock> {
        val container = Jsoup.parse("<article>$body</article>").select("article").first()!!
        return AvanteBodyParser.parseBodyContainer(container)
    }

    @Test
    fun onlyStrong_nestedEmphasis_isSubheading() {
        val blocks = parse("<p><strong><em>Título</em></strong></p>")

        assertEquals(listOf(ArticleBlock.Subheading("Título")), blocks)
    }

    @Test
    fun onlyStrong_surroundingWhitespace_isSubheading() {
        val blocks = parse("<p>  <strong>Título</strong>  </p>")

        assertEquals(listOf(ArticleBlock.Subheading("Título")), blocks)
    }

    @Test
    fun onlyStrong_withSiblingBr_isText() {
        val blocks = parse("<p><strong>Título</strong><br></p>")

        assertEquals(listOf(ArticleBlock.Text("Título")), blocks)
    }

    @Test
    fun onlyStrong_withOwnText_isText() {
        val blocks = parse("<p><strong>Lisboa</strong> — o resto do parágrafo.</p>")

        assertEquals(listOf(ArticleBlock.Text("Lisboa — o resto do parágrafo.")), blocks)
    }

    @Test
    fun onlyStrong_over100Chars_isText() {
        val long = "a".repeat(101)
        val blocks = parse("<p><strong>$long</strong></p>")

        assertEquals(listOf(ArticleBlock.Text(long)), blocks)
    }

    @Test
    fun onlyStrong_exactly100Chars_isSubheading() {
        val edge = "a".repeat(100)
        val blocks = parse("<p><strong>$edge</strong></p>")

        assertEquals(listOf(ArticleBlock.Subheading(edge)), blocks)
    }

    @Test
    fun headingsOutsideH3H4_areNotCandidates() {
        val blocks = parse("<h2>Fora</h2><h5>Também fora</h5><p>Texto</p>")

        assertEquals(listOf(ArticleBlock.Text("Texto")), blocks)
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
