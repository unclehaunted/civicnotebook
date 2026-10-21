package pt.avante.app.data.db

import org.junit.Assert.assertEquals
import org.junit.Test

/** Seam: o resumo que o cartão de Nota mostra, tirado do markdown do corpo. */
class NoteSummaryTest {

    @Test
    fun `o Excerto e a primeira citacao sem o marcador e sem espacos a volta`() {
        val summary = NoteSummary.of("Texto antes.\n>   Primeira citação.  \n> Segunda citação.")

        assertEquals("Primeira citação.", summary.excerpt)
    }

    @Test
    fun `a primeira linha salta linhas vazias, citacoes, titulos e linhas de etiquetas`() {
        val summary = NoteSummary.of("\n# Título\n#etiqueta\n> Citação.\n   \nO que penso.\nDepois.")

        assertEquals("O que penso.", summary.firstLine)
    }

    @Test
    fun `as etiquetas sao as palavras com cardinal, sem repetir, pela ordem`() {
        val summary = NoteSummary.of("# Título\nSobre #habitação e #luta_2026.\n#habitação outra vez, #pré-aviso.")

        assertEquals(listOf("#habitação", "#luta_2026", "#pré-aviso"), summary.tags)
    }

    @Test
    fun `corpo vazio nao tem Excerto, primeira linha nem etiquetas`() {
        val summary = NoteSummary.of("")

        assertEquals(NoteSummary(excerpt = null, firstLine = null, tags = emptyList()), summary)
    }
}
