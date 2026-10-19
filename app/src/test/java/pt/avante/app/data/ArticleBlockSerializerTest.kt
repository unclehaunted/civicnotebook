package pt.avante.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ArticleBlockSerializerTest {

    @Test
    fun blocosComQuestion_fazemRoundTrip() {
        val blocks = listOf(
            ArticleBlock.Subheading("Entrevista"),
            ArticleBlock.Question("Porquê militar no PCP?"),
            ArticleBlock.Text("Porque acredito num futuro melhor."),
            ArticleBlock.Quote("Citação"),
            ArticleBlock.PaywallCTA
        )

        val json = ArticleBlockSerializer.serialize(blocks)

        assertEquals(blocks, ArticleBlockSerializer.deserialize(json))
    }

    @Test
    fun jsonAntigoSemQuestion_continuaADesserializar() {
        val jsonEmCache = """
            [
              {"type":"pt.avante.app.data.ArticleBlock.Subheading","text":"Título"},
              {"type":"pt.avante.app.data.ArticleBlock.Text","content":"Um parágrafo."},
              {"type":"pt.avante.app.data.ArticleBlock.Quote","content":"Uma citação."},
              {"type":"pt.avante.app.data.ArticleBlock.Image","url":"https://x/y.jpg","caption":"Legenda"},
              {"type":"pt.avante.app.data.ArticleBlock.PaywallCTA"}
            ]
        """.trimIndent()

        assertEquals(
            listOf(
                ArticleBlock.Subheading("Título"),
                ArticleBlock.Text("Um parágrafo."),
                ArticleBlock.Quote("Uma citação."),
                ArticleBlock.Image("https://x/y.jpg", "Legenda"),
                ArticleBlock.PaywallCTA
            ),
            ArticleBlockSerializer.deserialize(jsonEmCache)
        )
    }
}
