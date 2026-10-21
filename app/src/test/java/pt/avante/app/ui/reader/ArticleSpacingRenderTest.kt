package pt.avante.app.ui.reader

import android.text.Layout
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
                            next = blocks.getOrNull(i + 1),
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

    // #13, story 9: sem caixa nem borda, o espaço à volta da Citação é só o do Espaçamento.
    @Test
    fun `antes da Citação 1,02 parágrafos e depois 0,98`() {
        val (before, quote, after) = renderAll(
            listOf(ArticleBlock.Text("Antes."), ArticleBlock.Quote("A luta continua."), ArticleBlock.Text("Depois.")),
            ReaderSettings()
        )

        assertEquals(spToPx(20.655f), gap(before, quote).toFloat(), PX_TOLERANCE) // 20,25 × 1,02
        assertEquals(spToPx(19.845f), gap(quote, after).toFloat(), PX_TOLERANCE) // 20,25 × 0,98
    }

    /** Topo de [view], em px da janela. */
    private fun top(view: View): Int = IntArray(2).also { view.getLocationInWindow(it) }[1]

    private fun contentTop(): Int =
        top(composeRule.activity.findViewById<View>(android.R.id.content))

    // O espaço entre dois blocos depende do par: antes da Citação fica o seu espaço próprio,
    // venha ela depois de um parágrafo, de uma Pergunta ou de outra Citação.
    @Test
    fun `a Citação depois de uma Pergunta fica a 1,02 parágrafos dela`() {
        val (question, quote) = renderAll(
            listOf(ArticleBlock.Question("Porquê?"), ArticleBlock.Quote("A luta continua.")),
            ReaderSettings()
        )

        assertEquals(spToPx(20.655f), gap(question, quote).toFloat(), PX_TOLERANCE) // 20,25 × 1,02
    }

    @Test
    fun `a Citação depois de outra Citação fica a 1,02 parágrafos dela`() {
        // Extra grande Arejado: o maior parágrafo, para 0,02 parágrafos passarem a tolerância.
        val settings = ReaderSettings(fontSize = ReaderFontSize.XLARGE, spacing = ReaderSpacing.AIRY)

        val (first, second) = renderAll(
            listOf(ArticleBlock.Quote("A luta continua."), ArticleBlock.Quote("E continuará.")),
            settings
        )

        assertEquals(spToPx(39.168f), gap(first, second).toFloat(), PX_TOLERANCE) // 24 × 1,6 × 1 × 1,02
    }

    // Sem bloco acima não há espaço entre blocos: a Citação começa onde começaria um parágrafo.
    @Test
    fun `a Citação como primeiro bloco não leva espaço antes`() {
        val quote = renderAll(listOf(ArticleBlock.Quote("A luta continua.")), ReaderSettings()).single()

        assertEquals(contentTop(), top(quote))
    }

    // #13, story 9: a Citação marca-se pelo recuo de 1,5 em à esquerda; à direita alinha com o corpo.
    @Test
    fun `a Citação recua 1,5 vezes o corpo à esquerda e acaba onde acaba o corpo`() {
        val (text, quote) = renderAll(
            // Textos de várias linhas, para os dois ocuparem a largura que têm.
            listOf(ArticleBlock.Text(LONG_TEXT), ArticleBlock.Quote(LONG_TEXT)),
            ReaderSettings(fontSize = ReaderFontSize.LARGE)
        )

        val textBox = IntArray(2).also { text.getLocationInWindow(it) }
        val quoteBox = IntArray(2).also { quote.getLocationInWindow(it) }
        assertEquals(spToPx(31.5f), (quoteBox[0] - textBox[0]).toFloat(), PX_TOLERANCE) // 21 × 1,5
        assertEquals(textBox[0] + text.width, quoteBox[0] + quote.width)
    }

    // #13: o subtítulo pertence ao texto que abre. Antes dele, o parágrafo do bloco anterior e
    // mais um; depois, um parágrafo (como no protótipo aprovado em #7). Segue o Espaçamento.
    @Test
    fun `antes do subtítulo dois parágrafos e depois um`() {
        val settings = ReaderSettings(fontSize = ReaderFontSize.SMALL, spacing = ReaderSpacing.AIRY)

        val (before, after) = renderAll(
            listOf(ArticleBlock.Text("Antes."), ArticleBlock.Subheading("Subtítulo"), ArticleBlock.Text("Depois.")),
            settings
        )

        // O subtítulo é texto Compose: os seus limites vêm na raiz Compose, em dp; passam-se a px
        // da janela para comparar com os TextViews.
        val subheading = composeRule.onNodeWithText("Subtítulo").getBoundsInRoot()
        val content = composeRule.activity.findViewById<View>(android.R.id.content)
        val rootTop = IntArray(2).also { content.getLocationInWindow(it) }[1]
        val density = composeRule.activity.resources.displayMetrics.density
        val top = rootTop + subheading.top.value * density
        val bottom = rootTop + subheading.bottom.value * density
        val beforeBottom = IntArray(2).also { before.getLocationInWindow(it) }[1] + before.height
        val afterTop = IntArray(2).also { after.getLocationInWindow(it) }[1]

        assertEquals(spToPx(52.8f), top - beforeBottom, PX_TOLERANCE) // 2 × 16 × 1,65 × 1
        assertEquals(spToPx(26.4f), afterTop - bottom, PX_TOLERANCE) // 16 × 1,65 × 1
    }

    @Test
    fun `o subtítulo depois de uma Pergunta fica a dois parágrafos dela`() {
        val settings = ReaderSettings(fontSize = ReaderFontSize.SMALL, spacing = ReaderSpacing.AIRY)

        val question = renderAll(
            listOf(ArticleBlock.Question("Porquê?"), ArticleBlock.Subheading("Subtítulo")),
            settings
        ).single()

        val subheading = composeRule.onNodeWithText("Subtítulo").getBoundsInRoot()
        val density = composeRule.activity.resources.displayMetrics.density
        val subheadingTop = contentTop() + subheading.top.value * density

        val questionBottom = top(question) + question.height
        assertEquals(spToPx(52.8f), subheadingTop - questionBottom, PX_TOLERANCE) // 2 × 16 × 1,65 × 1
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
