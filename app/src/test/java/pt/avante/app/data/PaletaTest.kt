package pt.avante.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Resolver da Aparência (#8): a única fonte de cor da app (ADR 0001). */
class PaletaTest {

    @Test
    fun cadaAparencia_defineCadaPapel_comUmaCorOpaca() {
        for (aparencia in ReaderTheme.entries) {
            val paleta = paleta(aparencia)
            for (papel in PapelDeCor.entries) {
                val alfa = paleta.cor(papel) ushr 24
                assertEquals("$aparencia/$papel opaco", 0xFFL, alfa)
            }
        }
    }

    // Veredito da #7: a Citação tem cor própria, um vermelho da família do acento.
    @Test
    fun aCitacao_eVermelha_ediferenteDoTextoPrimario() {
        for (aparencia in ReaderTheme.entries) {
            val paleta = paleta(aparencia)
            assertNotEquals("$aparencia", paleta.textoPrimario, paleta.citacao)
            assertTrue("$aparencia: Citação vermelha (${paleta.citacao.toString(16)})", vermelho(paleta.citacao))
        }
    }

    /** Matiz a menos de 15° do vermelho puro e saturação HSV de pelo menos 0,4. */
    private fun vermelho(argb: Long): Boolean {
        val (r, g, b) = canais(argb)
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        if (max != r || max == min) return false
        val matiz = 60.0 * (g - b) / (max - min)
        val saturacao = (max - min).toDouble() / max
        return kotlin.math.abs(matiz) < 15 && saturacao >= 0.4
    }

    // Nas Aparências claras é um vermelho escuro: mais escuro que o acento.
    @Test
    fun aCitacao_eMaisEscuraQueOAcento_nasAparenciasClaras() {
        for (aparencia in listOf(ReaderTheme.PAPER, ReaderTheme.WHITE, ReaderTheme.SEPIA)) {
            val paleta = paleta(aparencia)
            assertTrue("$aparencia", soma(paleta.citacao) < soma(paleta.acento))
        }
    }

    // Valores aprovados no protótipo (#7, Candidatos.kt em ebe6748).
    @Test
    fun oAcento_eOVermelhoAprovadoEmCadaAparencia() {
        assertEquals(0xFFB00020, paleta(ReaderTheme.PAPER).acento)
        assertEquals(0xFFB00020, paleta(ReaderTheme.WHITE).acento)
        assertEquals(0xFFA51D2D, paleta(ReaderTheme.SEPIA).acento)
        assertEquals(0xFFFF8A80, paleta(ReaderTheme.DARK).acento)
    }

    // ADR 0001: o esquema escuro genérico (#121212) e o Escuro de leitura (#1B1B1B) são um só.
    @Test
    fun soOEscuro_eUmaAparenciaEscura_comOFundoDeLeitura() {
        assertEquals(listOf(ReaderTheme.DARK), ReaderTheme.entries.filter { paleta(it).escura })
        assertEquals(0xFF1B1B1B, paleta(ReaderTheme.DARK).fundo)
    }

    private fun canais(argb: Long) = Triple((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)

    private fun soma(argb: Long) = canais(argb).let { (r, g, b) -> r + g + b }
}
