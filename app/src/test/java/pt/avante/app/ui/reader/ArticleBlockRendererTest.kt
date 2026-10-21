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
import pt.avante.app.data.ReaderFontSize
import pt.avante.app.data.ReaderSettings
import pt.avante.app.data.ReaderTheme
import pt.avante.app.data.TiposDeLetra
import pt.avante.app.data.paleta
import pt.avante.app.theme.OriginalTheme
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
            // Como na MainActivity: a Aparência escolhida dá o tema da app inteira.
            CompositionLocalProvider(LocalReaderSettings provides settings) {
              OriginalTheme(aparencia = settings.theme) {
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
            tiposDeLetra = TiposDeLetra.LEGIVEL,
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
        assertEquals(0xFFE3E0DB.toInt(), question.currentTextColor)
        assertEquals(text.currentTextColor, question.currentTextColor)
        assertSame(font(R.font.atkinson_hyperlegible_regular), text.typeface)
        assertSame(font(R.font.atkinson_hyperlegible_700), question.typeface)
    }

    // Veredito da #7: a Citação tem cor própria (o papel Citação), diferente do corpo, em cada Aparência.
    @Test
    fun citacao_usaACorDaCitacao_naAparenciaEscolhida() {
        val settings = ReaderSettings(theme = ReaderTheme.SEPIA)

        val (quote, text) = renderAll(
            listOf(ArticleBlock.Quote("A luta continua."), ArticleBlock.Text("Disse o orador.")),
            settings
        )

        assertEquals(paleta(ReaderTheme.SEPIA).citacao.toInt(), quote.currentTextColor)
        assertEquals(paleta(ReaderTheme.SEPIA).textoPrimario.toInt(), text.currentTextColor)
    }

    // Negrito real: onde a fonte tem ficheiro a 700, usa-se esse e não negrito sintetizado.
    @Test
    fun question_withAtkinson_usesItsBoldFile() = assertUsesBoldFile(TiposDeLetra.LEGIVEL, R.font.atkinson_hyperlegible_700)

    @Test
    fun question_withBitter_usesItsBoldFile() = assertUsesBoldFile(TiposDeLetra.LIVRO, R.font.bitter_700)

    private fun assertUsesBoldFile(tiposDeLetra: TiposDeLetra, boldRes: Int) {
        val view = render(ArticleBlock.Question("Porquê?"), settings = ReaderSettings(tiposDeLetra = tiposDeLetra))

        assertSame(font(boldRes), view.typeface)
        assertFalse(view.paint.isFakeBoldText)
    }

    // #6, story 2: a Pergunta usa o negrito da própria fonte do corpo (eixo wght), não o Bitter 700.
    @Test
    fun question_withLibreFranklin_usesItsOwnBold() = assertUsesBoldFile(TiposDeLetra.CLASSICO, R.font.libre_franklin_bold)

    // #6, story 1: a Citação usa o itálico desenhado da fonte, não o regular inclinado.
    @Test
    fun quote_withAtkinson_usesItsItalicFile() = assertUsesItalicFile(TiposDeLetra.LEGIVEL, R.font.atkinson_hyperlegible_italic)

    @Test
    fun quote_withBitter_usesItsItalic() = assertUsesItalicFile(TiposDeLetra.LIVRO, R.font.bitter_italic)

    @Test
    fun quote_withLibreFranklin_usesItsItalic() = assertUsesItalicFile(TiposDeLetra.CLASSICO, R.font.libre_franklin_italic)

    private fun assertUsesItalicFile(tiposDeLetra: TiposDeLetra, italicRes: Int) {
        val settings = ReaderSettings(tiposDeLetra = tiposDeLetra)
        val view = render(ArticleBlock.Quote("A luta continua."), settings = settings)

        assertSame(font(italicRes), view.typeface)
        assertEquals(0f, view.paint.textSkewX, 0f)
    }

    // As fontes variáveis abrem na instância Thin: o itálico tem de vir com o peso do texto.
    @Test
    fun quote_withBitter_hasTextWeightNotTheThinDefault() {
        val settings = ReaderSettings(tiposDeLetra = TiposDeLetra.LIVRO)
        val view = render(ArticleBlock.Quote("A luta continua."), settings = settings)

        assertTrue(ink(view.typeface) > ink(font(R.font.bitter_italic_vf)) * 1.5f)
    }

    @Test
    fun libreFranklin_textIsTheRegularWeightAndQuestionIsHeavier() {
        val (question, text, quote) = renderAll(
            listOf(ArticleBlock.Question("Porquê?"), ArticleBlock.Text("Entrei."), ArticleBlock.Quote("A luta.")),
            ReaderSettings(tiposDeLetra = TiposDeLetra.CLASSICO)
        )

        assertTrue(ink(text.typeface) > ink(font(R.font.libre_franklin_vf)) * 1.5f)
        assertTrue(ink(quote.typeface) > ink(font(R.font.libre_franklin_italic_vf)) * 1.5f)
        assertTrue(ink(question.typeface) > ink(text.typeface) * 1.2f)
    }

    /** Píxeis pintados por uma amostra de texto: mede o peso do traço que o leitor vê. */
    private fun ink(typeface: Typeface): Int {
        val bitmap = android.graphics.Bitmap.createBitmap(600, 80, android.graphics.Bitmap.Config.ARGB_8888)
        val paint = android.graphics.Paint().apply {
            this.typeface = typeface
            textSize = 48f
            color = android.graphics.Color.BLACK
        }
        android.graphics.Canvas(bitmap).drawText("Avante camaradas", 0f, 60f, paint)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        return pixels.count { android.graphics.Color.alpha(it) > 128 }
    }

    @Test
    fun question_withSourceSans_usesItsOwnBold() = assertUsesBoldFile(TiposDeLetra.MODERNO, R.font.source_sans_4_bold)

    @Test
    fun quote_withSourceSans_usesItsItalic() = assertUsesItalicFile(TiposDeLetra.MODERNO, R.font.source_sans_4_italic)

    @Test
    fun sourceSans_textIsTheRegularWeightAndQuestionIsHeavier() {
        val (question, text, quote) = renderAll(
            listOf(ArticleBlock.Question("Porquê?"), ArticleBlock.Text("Entrei."), ArticleBlock.Quote("A luta.")),
            ReaderSettings(tiposDeLetra = TiposDeLetra.MODERNO)
        )

        assertTrue(ink(text.typeface) > ink(font(R.font.source_sans_vf)) * 1.5f)
        assertTrue(ink(quote.typeface) > ink(font(R.font.source_sans_italic_vf)) * 1.5f)
        assertTrue(ink(question.typeface) > ink(text.typeface) * 1.2f)
    }
}
