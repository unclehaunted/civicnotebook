package pt.avante.app.theme

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.avante.app.data.ReaderTheme
import pt.avante.app.data.paleta

/** A app inteira segue a Aparência escolhida (ADR 0001): o chrome e o artigo têm uma só fonte de cor. */
@RunWith(RobolectricTestRunner::class)
class OriginalThemeTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    // O esquema escuro genérico (#121212) deixou de existir: em Escuro o chrome usa o fundo do artigo.
    @Test
    fun emEscuro_oChromeUsaOFundoDoArtigo() {
        var fundoDoChrome = Color.Unspecified
        var fundoDoArtigo = Color.Unspecified
        rule.setContent {
            OriginalTheme(aparencia = ReaderTheme.DARK) {
                fundoDoChrome = MaterialTheme.colorScheme.background
                fundoDoArtigo = Cores.fundo
            }
        }
        rule.waitForIdle()

        assertEquals(Color(0xFF1B1B1B), fundoDoChrome)
        assertEquals(fundoDoChrome, fundoDoArtigo)
    }

    // Cada papel chega à UI com o valor do resolver, em cada Aparência.
    @Test
    fun osPapeisDaUI_saoOsDoResolver() {
        val lidos = mutableMapOf<ReaderTheme, List<Color>>()
        val acentos = mutableMapOf<ReaderTheme, Color>()
        rule.setContent {
            for (aparencia in ReaderTheme.entries) {
                OriginalTheme(aparencia = aparencia) {
                    lidos[aparencia] = listOf(
                        Cores.fundo, Cores.superficie, Cores.cartao, Cores.divisor,
                        Cores.textoPrimario, Cores.textoSecundario, Cores.acento, Cores.sobreAcento,
                        Cores.destaque, Cores.citacao, Cores.escrita
                    )
                    acentos[aparencia] = MaterialTheme.colorScheme.primary
                }
            }
        }
        rule.waitForIdle()

        for (aparencia in ReaderTheme.entries) {
            val p = paleta(aparencia)
            val esperados = listOf(
                p.fundo, p.superficie, p.cartao, p.divisor, p.textoPrimario, p.textoSecundario,
                p.acento, p.sobreAcento, p.destaque, p.citacao, p.escrita
            ).map { Color(it) }
            assertEquals("$aparencia", esperados, lidos[aparencia])
            assertEquals("$aparencia", Color(p.acento), acentos[aparencia])
        }
    }
}
