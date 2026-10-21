package pt.avante.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Resolver da Aparência (#8): a única fonte de cor da app (ADR 0001). */
class PaletaTest {

    @Test
    fun cadaAparencia_defineCadaPapel_comUmaCorOpaca() {
        for (aparencia in APARENCIAS_CONCRETAS) {
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
        for (aparencia in APARENCIAS_CONCRETAS) {
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
        assertEquals(listOf(ReaderTheme.DARK), APARENCIAS_CONCRETAS.filter { paleta(it).escura })
        assertEquals(0xFF1B1B1B, paleta(ReaderTheme.DARK).fundo)
    }

    // #9: cada par texto/fundo que a app desenha cumpre WCAG AA (4.5:1, texto corrido) em cada
    // Aparência. Os ícones e contornos pousam nas mesmas cores, por isso os 3:1 dos elementos de UI
    // ficam cobertos. O divisor é decorativo e fica de fora.
    @Test
    fun cadaParDeTextoSobreFundo_cumpreAA_emCadaAparencia() {
        val falhas = APARENCIAS_CONCRETAS.flatMap { aparencia ->
            val paleta = paleta(aparencia)
            PARES_DE_TEXTO.mapNotNull { (texto, fundo) ->
                val razao = contraste(paleta.cor(texto), paleta.cor(fundo))
                "$aparencia: $texto sobre $fundo = %.2f".format(razao).takeIf { razao < AA_TEXTO }
            }
        }
        assertEquals(emptyList<String>(), falhas)
    }

    // WCAG 1.4.11: o contorno de um componente (o campo de pesquisa, em repouso e com foco) é o
    // que o identifica, por isso fica a 3:1 de cada fundo em cada Aparência.
    @Test
    fun oContornoDosComponentes_cumpre3para1_emCadaAparencia() {
        val falhas = APARENCIAS_CONCRETAS.flatMap { aparencia ->
            val paleta = paleta(aparencia)
            listOf("contorno" to paleta.contorno, "contorno com foco" to paleta.acento).flatMap { (nome, cor) ->
                FUNDOS_DE_TEXTO.mapNotNull { fundo ->
                    val razao = contraste(cor, paleta.cor(fundo))
                    "$aparencia: $nome sobre $fundo = %.2f".format(razao).takeIf { razao < AA_UI }
                }
            }
        }
        assertEquals(emptyList<String>(), falhas)
    }

    @Test
    fun oContorno_eOpaco_emCadaAparencia() {
        for (aparencia in APARENCIAS_CONCRETAS) {
            assertEquals("$aparencia", 0xFFL, paleta(aparencia).contorno ushr 24)
        }
    }

    // Decisão do maintainer na #5: Automática segue o modo escuro do sistema, Papel de dia e
    // Escuro de noite, com as cores dessas duas e nenhuma própria.
    @Test
    fun aAutomatica_ePapelDeDia_eEscuroDeNoite() {
        assertEquals(paleta(ReaderTheme.PAPER), paleta(ReaderTheme.AUTOMATIC, sistemaEscuro = false))
        assertEquals(paleta(ReaderTheme.DARK), paleta(ReaderTheme.AUTOMATIC, sistemaEscuro = true))
    }

    // O modo do sistema só conta para a Automática: uma Aparência escolhida fica a mesma.
    @Test
    fun umaAparenciaEscolhida_ignoraOModoDoSistema() {
        for (aparencia in APARENCIAS_CONCRETAS) {
            assertEquals(
                "$aparencia",
                paleta(aparencia, sistemaEscuro = false),
                paleta(aparencia, sistemaEscuro = true)
            )
        }
    }

    // A fórmula de luminância relativa da WCAG 2.x, conferida em valores conhecidos.
    @Test
    fun oContraste_segueAFormulaDaWcag() {
        assertEquals(21.0, contraste(0xFF000000, 0xFFFFFFFF), 0.01)
        assertEquals(1.0, contraste(0xFF1B1B1B, 0xFF1B1B1B), 0.001)
        // #767676 é o cinzento mais claro que passa AA sobre branco.
        assertEquals(4.54, contraste(0xFFFFFFFF, 0xFF767676), 0.01)
        assertTrue(contraste(0xFFFFFFFF, 0xFF777777) < AA_TEXTO)
    }

    private fun contraste(a: Long, b: Long): Double {
        val (clara, escura) = listOf(luminancia(a), luminancia(b)).sortedDescending()
        return (clara + 0.05) / (escura + 0.05)
    }

    private fun luminancia(argb: Long): Double {
        val (r, g, b) = canais(argb).toList().map { linear(it / 255.0) }
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    private fun linear(canal: Double) =
        if (canal <= 0.04045) canal / 12.92 else Math.pow((canal + 0.055) / 1.055, 2.4)

    private fun canais(argb: Long) = Triple((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)

    private fun soma(argb: Long) = canais(argb).let { (r, g, b) -> r + g + b }

    private companion object {
        /** As que têm cores próprias; a Automática empresta as de Papel ou de Escuro. */
        val APARENCIAS_CONCRETAS = ReaderTheme.entries - ReaderTheme.AUTOMATIC

        const val AA_TEXTO = 4.5

        /** Elementos gráficos e contornos de componentes (WCAG 1.4.11). */
        const val AA_UI = 3.0

        val FUNDOS_DE_TEXTO = listOf(PapelDeCor.FUNDO, PapelDeCor.SUPERFICIE, PapelDeCor.CARTAO)

        /**
         * Texto sobre os fundos: os papéis de texto (o acento também é texto: "Eliminar",
         * "Ver Artigo"); sobre o Destaque, a passagem destacada (corpo e Citação) e o subtítulo
         * de "Realçar excerto"; sobre o acento, os rótulos e ícones das ações.
         */
        val PARES_DE_TEXTO: List<Pair<PapelDeCor, PapelDeCor>> =
            listOf(
                PapelDeCor.TEXTO_PRIMARIO, PapelDeCor.TEXTO_SECUNDARIO, PapelDeCor.ACENTO,
                PapelDeCor.CITACAO, PapelDeCor.ESCRITA
            ).flatMap { texto -> FUNDOS_DE_TEXTO.map { texto to it } } +
                listOf(PapelDeCor.TEXTO_PRIMARIO, PapelDeCor.TEXTO_SECUNDARIO, PapelDeCor.CITACAO)
                    .map { it to PapelDeCor.DESTAQUE } +
                (PapelDeCor.SOBRE_ACENTO to PapelDeCor.ACENTO)
    }
}
