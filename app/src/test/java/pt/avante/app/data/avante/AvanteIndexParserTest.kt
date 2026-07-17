package pt.avante.app.data.avante

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Test
import pt.avante.app.data.ArticleSummary

class AvanteIndexParserTest {

    @Test
    fun `extractArticleFromIndexElement extracts local h5 overriding external rubric`() {
        val html = """
            <div class="views-row">
                <article>
                    <em><h5>A talhe de foice</h5></em>
                    <h2>Título do artigo</h2>
                    <div class="field-name-body">Resumo do artigo</div>
                    <div class="field-name-field-authors">João</div>
                    <div class="field-name-field-image">
                        <img src="/image.jpg" />
                    </div>
                    <ul class="links">
                        <li class="node-readmore"><a href="/artigo/123.htm">Ler mais</a></li>
                    </ul>
                </article>
            </div>
        """.trimIndent()

        val doc = Jsoup.parse(html)
        val element = doc.selectFirst("article")!!

        val inventory = mutableMapOf<String, pt.avante.app.data.avante.IndexMetadata>()
        AvanteIndexParser.extractArticleFromIndexElement(
            article = element,
            currentCategory = "Opinião",
            currentSubcategory = null,
            inventory = inventory
        )
        
        println("Inventory: $inventory")
        val parsed = inventory.values.firstOrNull()

        assertEquals("A talhe de foice", parsed?.subcategoryLabel)
        assertEquals("Opinião", parsed?.categoryLabel)
    }
}
