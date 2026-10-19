package pt.avante.app.ui.reader

import android.graphics.Typeface
import android.text.Selection
import android.text.Spannable
import android.view.ActionMode
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
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
import pt.avante.app.data.ArticleBlock

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
        onCreateNote: (String, Int, Int, Int) -> Unit = { _, _, _, _ -> }
    ): TextView {
        composeRule.setContent {
            ArticleBlockRenderer(
                block = block,
                index = index,
                isSelected = false,
                articleHighlights = emptyList(),
                onHighlightClick = {},
                onHighlightPassage = { _, _, _, _ -> },
                onCreateNote = onCreateNote
            )
        }
        composeRule.waitForIdle()
        return composeRule.activity.window.decorView.findTextViews().single()
    }

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
        whenever(item.itemId).thenReturn(2) // "Criar Nota"
        val handled = view.customSelectionActionModeCallback.onActionItemClicked(mock<ActionMode>(), item)

        assertTrue(handled)
        assertEquals(listOf("Porquê", 3, 0, 6), note)
    }
}
