package pt.avante.app.ui.reader

import android.text.Layout
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.Publication
import pt.avante.app.data.ReaderFontSize
import pt.avante.app.data.ReaderSettings
import pt.avante.app.data.ReaderSpacing
import pt.avante.app.ui.settings.LocalReaderSettings
import kotlin.math.roundToInt

/**
 * Seam 2 (#12): o Espaçamento e a hifenização no texto do artigo, observados pelo que o Leitor
 * vê: a distância entre linhas, a distância entre blocos e a hifenização portuguesa.
 * Densidade xxhdpi (3 px por dp) para as diferenças de meio ponto serem mensuráveis.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "xxhdpi")
class ArticleSpacingRenderTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun renderAll(blocks: List<ArticleBlock>, settings: ReaderSettings): List<TextView> {
        composeRule.setContent {
            CompositionLocalProvider(LocalReaderSettings provides settings) {
                Column {
                    blocks.forEachIndexed { i, block ->
                        ArticleBlockRenderer(
                            block = block,
                            index = i,
                            isSelected = false,
                            articleHighlights = emptyList(),
                            onHighlightClick = {},
                            onHighlightPassage = { _, _, _, _ -> },
                            onCreateNote = { _, _, _, _ -> }
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        return composeRule.activity.window.decorView.findTextViews()
    }

    private fun View.findTextViews(): List<TextView> = when (this) {
        is TextView -> listOf(this)
        is ViewGroup -> (0 until childCount).flatMap { getChildAt(it).findTextViews() }
        else -> emptyList()
    }

    private fun spToPx(sp: Float): Float = sp * composeRule.activity.resources.displayMetrics.scaledDensity

    @Test
    fun `em Medio Normal a linha mede 1,5 vezes o corpo`() {
        val view = renderAll(listOf(ArticleBlock.Text(LONG_TEXT)), ReaderSettings()).single()

        assertEquals(spToPx(18f * 1.5f).roundToInt(), view.lineHeight)
    }

    @Test
    fun `em Extra grande Compacto a linha mede 1,3 vezes o corpo`() {
        val settings = ReaderSettings(fontSize = ReaderFontSize.XLARGE, spacing = ReaderSpacing.COMPACT)

        val view = renderAll(listOf(ArticleBlock.Text(LONG_TEXT)), settings).single()

        assertEquals(spToPx(24f * 1.3f).roundToInt(), view.lineHeight)
    }

    /** Espaço vertical, em px, entre o fim de um TextView e o início do seguinte. */
    private fun gap(above: TextView, below: TextView): Int {
        val a = IntArray(2).also { above.getLocationInWindow(it) }
        val b = IntArray(2).also { below.getLocationInWindow(it) }
        return b[1] - (a[1] + above.height)
    }

    @Test
    fun `entre parágrafos fica três quartos de linha em Medio Normal`() {
        val (first, second) = renderAll(
            listOf(ArticleBlock.Text("Primeiro parágrafo."), ArticleBlock.Text("Segundo parágrafo.")),
            ReaderSettings()
        )

        assertEquals(spToPx(20.25f), gap(first, second).toFloat(), PX_TOLERANCE) // 18 × 1,5 × ¾
    }

    @Test
    fun `entre parágrafos fica uma linha em Pequeno Arejado`() {
        val settings = ReaderSettings(fontSize = ReaderFontSize.SMALL, spacing = ReaderSpacing.AIRY)

        val (first, second) = renderAll(
            listOf(ArticleBlock.Text("Primeiro parágrafo."), ArticleBlock.Text("Segundo parágrafo.")),
            settings
        )

        assertEquals(spToPx(26.4f), gap(first, second).toFloat(), PX_TOLERANCE) // 16 × 1,65 × 1
    }

    @Test
    fun `a Pergunta fica a meio espaço de parágrafo da resposta`() {
        val (question, answer) = renderAll(
            listOf(ArticleBlock.Question("Porquê?"), ArticleBlock.Text("Porque sim.")),
            ReaderSettings()
        )

        assertEquals(spToPx(10.125f), gap(question, answer).toFloat(), PX_TOLERANCE)
    }

    // Veredito de #7: antes da Citação 1,02 × parágrafo, depois 0,98 ×. A diferença entre os dois
    // (0,04 × parágrafo) não depende do que a Citação desenha por dentro.
    @Test
    fun `a Citação tem mais espaço antes do que depois`() {
        val settings = ReaderSettings(fontSize = ReaderFontSize.XLARGE, spacing = ReaderSpacing.AIRY)

        val (before, quote, after) = renderAll(
            listOf(ArticleBlock.Text("Antes."), ArticleBlock.Quote("A luta continua."), ArticleBlock.Text("Depois.")),
            settings
        )

        val difference = gap(before, quote) - gap(quote, after)
        assertEquals(spToPx(38.4f * 0.04f), difference.toFloat(), PX_TOLERANCE) // parágrafo 24 × 1,6 × 1
    }

    /** Renderiza um artigo inteiro, como o leitor o mostra, e devolve o TextView do corpo. */
    private fun renderArticleBody(settings: ReaderSettings): TextView {
        val article = Article(
            id = "1",
            title = "Título",
            author = "Autor",
            publishedAt = "2026-10-21",
            canonicalUrl = "https://www.avante.pt/pt/1/",
            publication = Publication.AVANTE,
            blocks = listOf(ArticleBlock.Text(LONG_TEXT))
        )
        composeRule.setContent {
            CompositionLocalProvider(LocalReaderSettings provides settings) {
                ReaderBody(
                    article = article,
                    currentIssueNumber = null,
                    listState = rememberLazyListState(),
                    scrollOffset = 0f,
                    selectedPassageIndex = null,
                    articleHighlights = emptyList(),
                    onHighlightClick = {},
                    onHighlightPassage = { _, _, _, _ -> },
                    onCreateNote = { _, _, _, _ -> },
                    heroContent = {}
                )
            }
        }
        composeRule.waitForIdle()
        return composeRule.activity.window.decorView.findTextViews().single { it.text.startsWith("A organização") }
    }

    private fun dpToPx(dp: Float): Float = dp * composeRule.activity.resources.displayMetrics.density

    // Num tablet a coluna para nos 33 em do corpo (cerca de 66 caracteres), não nos 720dp.
    @Test
    @Config(qualifiers = "w1200dp-h800dp-xxhdpi")
    fun `num ecrã largo a coluna mede 33 vezes o corpo em Medio`() {
        val body = renderArticleBody(ReaderSettings())

        assertEquals(dpToPx(594f), body.width.toFloat(), PX_TOLERANCE) // 33 × 18
    }

    @Test
    @Config(qualifiers = "w1200dp-h800dp-xxhdpi")
    fun `num ecrã largo a coluna mede 33 vezes o corpo em Pequeno`() {
        val body = renderArticleBody(ReaderSettings(fontSize = ReaderFontSize.SMALL))

        assertEquals(dpToPx(528f), body.width.toFloat(), PX_TOLERANCE) // 33 × 16
    }

    @Test
    fun `o corpo hifeniza em português, alinhado à esquerda`() {
        val view = renderAll(listOf(ArticleBlock.Text(LONG_TEXT)), ReaderSettings()).single()

        assertEquals(Layout.HYPHENATION_FREQUENCY_NORMAL, view.hyphenationFrequency)
        assertEquals("pt", view.textLocale.language)
        assertEquals(Layout.JUSTIFICATION_MODE_NONE, view.justificationMode)
    }
}

/** Cada padding arredonda a px inteiros. */
private const val PX_TOLERANCE = 1.5f

private const val LONG_TEXT =
    "A organização dos trabalhadores nas empresas e locais de trabalho é condição indispensável " +
        "para a defesa dos seus interesses e direitos."
