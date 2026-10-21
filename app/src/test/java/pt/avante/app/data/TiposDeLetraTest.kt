package pt.avante.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Seam: a migração da preferência antiga de fonte única (`reader_font`) para os Tipos de letra
 * (#10, user story 4). A tabela vem da spec #5; IBM Plex Sans saiu da lista em #6.
 */
class TiposDeLetraTest {

    @Test
    fun `cada fonte antiga passa a combinacao mais proxima`() {
        val tabela = mapOf(
            "LIBRE_FRANKLIN" to TiposDeLetra.CLASSICO,
            "BITTER" to TiposDeLetra.LIVRO,
            "ATKINSON_HYPERLEGIBLE" to TiposDeLetra.LEGIVEL,
            "SOURCE_SANS" to TiposDeLetra.MODERNO,
            "IBM_PLEX_SANS" to TiposDeLetra.CLASSICO
        )

        tabela.forEach { (antiga, esperada) ->
            assertEquals(antiga, esperada, TiposDeLetra.daFonteAntiga(antiga))
        }
    }

    @Test
    fun `fonte antiga desconhecida ou ausente passa a Classico`() {
        assertEquals(TiposDeLetra.CLASSICO, TiposDeLetra.daFonteAntiga("COMIC_SANS"))
        assertEquals(TiposDeLetra.CLASSICO, TiposDeLetra.daFonteAntiga(null))
    }

    @Test
    fun `cada combinacao tem a face de display e a face de texto da spec`() {
        val faces = TiposDeLetra.entries.associateWith { it.display to it.texto }

        assertEquals(
            mapOf(
                TiposDeLetra.CLASSICO to (ReaderFont.BITTER to ReaderFont.LIBRE_FRANKLIN),
                TiposDeLetra.LIVRO to (ReaderFont.BITTER to ReaderFont.BITTER),
                TiposDeLetra.MODERNO to (ReaderFont.LIBRE_FRANKLIN to ReaderFont.SOURCE_SANS),
                TiposDeLetra.LEGIVEL to (ReaderFont.ATKINSON_HYPERLEGIBLE to ReaderFont.ATKINSON_HYPERLEGIBLE)
            ),
            faces
        )
    }
}
