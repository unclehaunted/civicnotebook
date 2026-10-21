package pt.avante.app.ui.components

import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.drawToBitmap
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.data.ReaderTheme
import pt.avante.app.data.paleta
import pt.avante.app.theme.Cores
import pt.avante.app.theme.OriginalTheme
import kotlin.math.abs

/** Diferença máxima por canal (0–255) para um píxel contar como da cor do texto. */
private const val TOLERANCIA_DE_COR = 24

/** As letras cobrem ~21% do logótipo; com letras pretas fixas, só as orlas chegavam a 1%. */
private const val FRACAO_MINIMA_DE_LETRAS = 0.10f

/**
 * Seam: o logótipo do masthead, observado pelos píxeis desenhados sobre o fundo da Aparência.
 * As letras são da cor do texto primário em todas as Aparências, para não ficarem escuras no Escuro.
 * Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h800dp-xxhdpi")
class MastheadLogotipoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    /** A fração dos píxeis do logótipo que têm a cor do texto primário da Aparência. */
    private fun fracaoDeLetras(aparencia: ReaderTheme): Float {
        composeRule.setContent {
            OriginalTheme(aparencia) {
                Box(Modifier.background(Cores.fundo)) {
                    AvanteMasthead(onMenuClick = {}, onSearchClick = {})
                }
            }
        }
        val texto = Color(paleta(aparencia).textoPrimario)
        // captureToImage espera por um frame da janela que o Robolectric não dá; desenha-se a vista.
        val limites = composeRule.onNodeWithContentDescription("Avante!").fetchSemanticsNode().boundsInRoot
        val raiz = composeRule.activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
        val ecra = raiz.drawToBitmap()
        var deLetras = 0
        var total = 0
        for (x in limites.left.toInt() until limites.right.toInt()) {
            for (y in limites.top.toInt() until limites.bottom.toInt()) {
                total++
                if (pertoDe(Color(ecra.getPixel(x, y)), texto)) deLetras++
            }
        }
        return deLetras.toFloat() / total
    }

    private fun pertoDe(a: Color, b: Color): Boolean =
        listOf(a.red - b.red, a.green - b.green, a.blue - b.blue).all { abs(it) * 255 <= TOLERANCIA_DE_COR }

    private fun assertLetrasNaCorDoTexto(aparencia: ReaderTheme) {
        val fracao = fracaoDeLetras(aparencia)
        assertTrue("$aparencia: só ${fracao * 100}% do logótipo na cor do texto", fracao > FRACAO_MINIMA_DE_LETRAS)
    }

    @Test
    fun `letras na cor do texto no Escuro`() = assertLetrasNaCorDoTexto(ReaderTheme.DARK)

    @Test
    fun `letras na cor do texto no Papel`() = assertLetrasNaCorDoTexto(ReaderTheme.PAPER)

    @Test
    fun `letras na cor do texto no Sepia`() = assertLetrasNaCorDoTexto(ReaderTheme.SEPIA)
}
