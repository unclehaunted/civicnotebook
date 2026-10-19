package pt.avante.app.ui.reader

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.avante.app.data.ArticleBlock

/** Story 12: a marca de fim do artigo cai no último bloco de texto selecionável. */
class EndMarkBlockIndexTest {

    @Test
    fun trailingQuestionTextPair_marksTheAnswer() {
        val blocks = listOf(
            ArticleBlock.Text("Introdução."),
            ArticleBlock.Question("Porquê militar no PCP?"),
            ArticleBlock.Text("Porque sim."),
        )

        assertEquals(2, endMarkBlockIndex(blocks))
    }

    @Test
    fun trailingQuestionWithoutAnswer_marksTheQuestion() {
        val blocks = listOf(ArticleBlock.Text("Introdução."), ArticleBlock.Question("E agora?"))

        assertEquals(1, endMarkBlockIndex(blocks))
    }

    @Test
    fun trailingNonTextBlocks_areSkipped() {
        val blocks = listOf(
            ArticleBlock.Question("Porquê?"),
            ArticleBlock.Text("Porque sim."),
            ArticleBlock.Image("https://exemplo.pt/a.jpg", "Legenda"),
            ArticleBlock.Subheading("Secção"),
        )

        assertEquals(1, endMarkBlockIndex(blocks))
    }

    @Test
    fun trailingQuote_countsAsText() {
        val blocks = listOf(ArticleBlock.Text("Texto."), ArticleBlock.Quote("Citação."))

        assertEquals(1, endMarkBlockIndex(blocks))
    }

    @Test
    fun paywall_hasNoEndMark() {
        val blocks = listOf(ArticleBlock.Question("Porquê?"), ArticleBlock.Text("Porque sim."), ArticleBlock.PaywallCTA)

        assertEquals(-1, endMarkBlockIndex(blocks))
    }

    @Test
    fun noTextBlocks_hasNoEndMark() {
        assertEquals(-1, endMarkBlockIndex(listOf(ArticleBlock.Subheading("Só título"))))
    }
}
