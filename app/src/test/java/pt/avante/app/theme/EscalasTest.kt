package pt.avante.app.theme

import org.junit.Assert.assertEquals
import org.junit.Test
import pt.avante.app.data.ReaderFontSize

/**
 * As escalas modulares de 1,25 (#11): a do artigo cresce com o Tamanho, a do chrome é fixa.
 * Os valores esperados são os da spec #5 e do protótipo aprovado em #7.
 */
class EscalasTest {

    @Test
    fun escalaDoArtigo_pequeno_daCorpo16Subtitulo20Titulo25() {
        assertArtigo(ReaderFontSize.SMALL, corpo = 16f, subtitulo = 20f, titulo = 25f)
    }

    @Test
    fun escalaDoArtigo_medio_daCorpo18Subtitulo22e5Titulo28() {
        assertArtigo(ReaderFontSize.MEDIUM, corpo = 18f, subtitulo = 22.5f, titulo = 28.125f)
    }

    @Test
    fun escalaDoArtigo_grande_daCorpo21Subtitulo26Titulo33() {
        assertArtigo(ReaderFontSize.LARGE, corpo = 21f, subtitulo = 26.25f, titulo = 32.8125f)
    }

    @Test
    fun escalaDoArtigo_extraGrande_daCorpo24Subtitulo30Titulo37e5() {
        assertArtigo(ReaderFontSize.XLARGE, corpo = 24f, subtitulo = 30f, titulo = 37.5f)
    }

    @Test
    fun escalaDoChrome_partede16_com12e8_16_20_25() {
        assertEquals(
            listOf(12.8f, 16f, 20f, 25f),
            DegrauDoChrome.entries.map { it.sp }
        )
    }

    @Test
    fun escalaDoChrome_nenhumDegrauAbaixoDe12sp() {
        DegrauDoChrome.entries.forEach { degrau ->
            assert(degrau.sp >= 12f) { "$degrau tem ${degrau.sp}sp" }
        }
    }

    private fun assertArtigo(tamanho: ReaderFontSize, corpo: Float, subtitulo: Float, titulo: Float) {
        val escala = escalaDoArtigo(tamanho)
        assertEquals("corpo", corpo, escala.corpo, TOLERANCIA)
        assertEquals("Pergunta", corpo, escala.pergunta, TOLERANCIA)
        assertEquals("Citação", corpo, escala.citacao, TOLERANCIA)
        assertEquals("subtítulo", subtitulo, escala.subtitulo, TOLERANCIA)
        assertEquals("título", titulo, escala.titulo, TOLERANCIA)
        assertEquals("título da Nota", subtitulo, escala.tituloDaNota, TOLERANCIA)
    }

    private companion object {
        const val TOLERANCIA = 0.001f
    }
}
