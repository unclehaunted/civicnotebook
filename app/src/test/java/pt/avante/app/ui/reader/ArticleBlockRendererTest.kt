package pt.avante.app.ui.reader

import android.graphics.Typeface
import android.text.Selection
import android.text.Spannable
import android.view.ActionMode
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.core.content.res.ResourcesCompat
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.R
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.ReaderFont
import pt.avante.app.data.ReaderFontSize
import pt.avante.app.data.ReaderSettings
import pt.avante.app.data.ReaderTheme
import pt.avante.app.ui.settings.LocalReaderSettings

/**
 * Seam 2 (spec #1): o renderer de blocos, observado pelo que o utilizador vê e pode fazer —
 * texto, peso e seleção. Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArticleBlockRendererTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun render(
        block: ArticleBlock,
        index: Int = 0,
        settings: ReaderSettings = ReaderSettings(),
        onCreateNote: (String, Int, Int, Int) -> Unit = { _, _, _, _ -> }
    ): TextView = renderAll(listOf(block), settings, index, onCreateNote).single()

    /** Renderiza os blocos numa coluna, como o ReaderBody faz, e devolve os TextViews pela ordem. */
    private fun renderAll(
        blocks: List<ArticleBlock>,
        settings: ReaderSettings = ReaderSettings(),
        firstIndex: Int = 0,
        onCreateNote: (String, Int, Int, Int) -> Unit = { _, _, _, _ -> }
    ): List<TextView> {
        composeRule.setContent {
            CompositionLocalProvider(LocalReaderSettings provides settings) {
                Column {
                    blocks.forEachIndexed { i, block ->
                        ArticleBlockRenderer(
                            block = block,
                            index = firstIndex + i,
                            isSelected = false,
                            articleHighlights = emptyList(),
                            onHighlightClick = {},
                            onHighlightPassage = { _, _, _, _ -> },
                            onCreateNote = onCreateNote
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        return composeRule.activity.window.decorView.findTextViews()
    }

    private fun font(resId: Int): Typeface = ResourcesCompat.getFont(composeRule.activity, resId)!!

    private fun View.findTextViews(): List<TextView> = when (this) {
        is TextView -> listOf(this)
        is ViewGroup -> (0 until childCount).flatMap { getChildAt(it).findTextViews() }
        else -> emptyList()
    }

    @Test
    fun question_mostraOTextoDaPergunta() {
        val view = render(ArticleBlock.Question("Porquê militar no PCP?"))

        assertEquals("Porquê militar no PCP?", view.text.toString())
    }

    @Test
    fun question_ficaANegrito() {
        val view = render(ArticleBlock.Question("Porquê militar no PCP?"))

        assertTrue(view.typeface.isBold)
    }

    @Test
    fun text_continuaSemNegrito() {
        val view = render(ArticleBlock.Text("Entrei no Partido aos dezoito anos."))

        assertFalse(view.typeface.isBold)
        assertEquals(Typeface.NORMAL, view.typeface.style)
    }

    @Test
    fun question_eSelecionavelComoText() {
        val question = render(ArticleBlock.Question("Porquê militar no PCP?"))

        assertTrue(question.isTextSelectable)
        assertNotNull(question.customSelectionActionModeCallback)
    }

    @Test
    fun question_criarNotaDevolveOTextoSelecionadoEOIndiceDaPergunta() {
        var note: List<Any>? = null
        val view = render(
            ArticleBlock.Question("Porquê militar no PCP?"),
            index = 3,
            onCreateNote = { text, index, start, end -> note = listOf(text, index, start, end) }
        )

        Selection.setSelection(view.text as Spannable, 0, 6)
        val item = mock<MenuItem>()
        whenever(item.itemId).thenReturn(SELECTION_MENU_CREATE_NOTE)
        val handled = view.customSelectionActionModeCallback.onActionItemClicked(mock<ActionMode>(), item)

        assertTrue(handled)
        assertEquals(listOf("Porquê", 3, 0, 6), note)
    }

    // Story 17: pergunta e resposta são elementos separados (um TextView/nó de acessibilidade cada).
    @Test
    fun questionAndFollowingText_areSeparateTextViews() {
        val views = renderAll(
            listOf(ArticleBlock.Question("Porquê militar no PCP?"), ArticleBlock.Text("Entrei aos dezoito anos."))
        )

        assertEquals(2, views.size)
        assertNotSame(views[0], views[1])
        assertEquals("Porquê militar no PCP?", views[0].text.toString())
        assertEquals("Entrei aos dezoito anos.", views[1].text.toString())
    }

    // Stories 15/16: a pergunta segue fonte, tamanho e cor do tema escolhidos nas Definições.
    @Test
    fun question_followsFontSizeAndThemeColour() {
        val settings = ReaderSettings(
            font = ReaderFont.ATKINSON_HYPERLEGIBLE,
            fontSize = ReaderFontSize.XLARGE,
            theme = ReaderTheme.DARK
        )

        val (question, text) = renderAll(
            listOf(ArticleBlock.Question("Porquê militar no PCP?"), ArticleBlock.Text("Entrei aos dezoito anos.")),
            settings
        )

        val expectedSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 24f, composeRule.activity.resources.displayMetrics
        )
        assertEquals(expectedSize, question.textSize, 0.01f)
        assertEquals(text.textSize, question.textSize, 0.01f)
        assertEquals(ReaderTheme.DARK.textColor.toInt(), question.currentTextColor)
        assertEquals(text.currentTextColor, question.currentTextColor)
        assertSame(font(R.font.atkinson_hyperlegible_regular), text.typeface)
        assertSame(font(R.font.atkinson_hyperlegible_700), question.typeface)
    }

    // Negrito real: onde a fonte tem ficheiro a 700, usa-se esse e não negrito sintetizado.
    @Test
    fun question_withAtkinson_usesItsBoldFile() = assertUsesBoldFile(ReaderFont.ATKINSON_HYPERLEGIBLE, R.font.atkinson_hyperlegible_700)

    @Test
    fun question_withBitter_usesItsBoldFile() = assertUsesBoldFile(ReaderFont.BITTER, R.font.bitter_700)

    private fun assertUsesBoldFile(readerFont: ReaderFont, boldRes: Int) {
        val view = render(ArticleBlock.Question("Porquê?"), settings = ReaderSettings(font = readerFont))

        assertSame(font(boldRes), view.typeface)
        assertFalse(view.paint.isFakeBoldText)
    }

    // Libre Franklin nunca aparece a negrito: a pergunta usa o Bitter 700 dos títulos.
    @Test
    fun question_withLibreFranklin_usesTitleBoldBitter() = assertUsesBoldFile(ReaderFont.LIBRE_FRANKLIN, R.font.bitter_700)

    @Test
    fun question_withoutBoldFontFile_fallsBackToSynthesizedBold() {
        val view = render(ArticleBlock.Question("Porquê?"), settings = ReaderSettings(font = ReaderFont.SOURCE_SANS))

        // Sem ficheiro a 700 o Android sintetiza o negrito a partir do tipo regular.
        assertTrue(view.typeface.isBold)
        assertNotSame(font(R.font.source_sans_4_regular), view.typeface)
    }
}
