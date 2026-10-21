package pt.avante.app.ui.reader

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.data.Publication
import pt.avante.app.data.ReaderFontSize
import pt.avante.app.data.ReaderSettings
import pt.avante.app.data.TiposDeLetra
import pt.avante.app.theme.AtkinsonHyperlegible
import pt.avante.app.theme.Bitter
import pt.avante.app.theme.OriginalTheme
import pt.avante.app.ui.settings.LocalReaderSettings

/** Seam 2 (#13): o título do artigo, pelo estilo com que o Compose o desenhou. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReaderHeroTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun renderedTitleStyle(settings: ReaderSettings): TextStyle {
        composeRule.setContent {
            CompositionLocalProvider(LocalReaderSettings provides settings) {
                OriginalTheme(aparencia = settings.theme) {
                    ReaderHero(
                        title = TITLE,
                        categoryLabel = null,
                        subcategoryLabel = null,
                        publication = Publication.AVANTE,
                        issueNumber = null,
                        author = "Ana Ribeiro",
                        displayImage = null,
                        currentIssueNumber = null
                    )
                }
            }
        }
        val results = mutableListOf<TextLayoutResult>()
        composeRule.onNodeWithText(TITLE).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        return results.single().layoutInput.style
    }

    // Story 1/#13: o título é da face de display dos Tipos de letra, a negrito.
    @Test
    fun title_inClassico_isTheDisplayFaceBitterBold() {
        val style = renderedTitleStyle(ReaderSettings(tiposDeLetra = TiposDeLetra.CLASSICO))

        assertEquals(Bitter, style.fontFamily)
        assertEquals(FontWeight.Bold, style.fontWeight)
    }

    @Test
    fun title_inLegivel_isTheDisplayFaceAtkinsonBold() {
        val style = renderedTitleStyle(ReaderSettings(tiposDeLetra = TiposDeLetra.LEGIVEL))

        assertEquals(AtkinsonHyperlegible, style.fontFamily)
        assertEquals(FontWeight.Bold, style.fontWeight)
    }

    // Story 5: o título cresce com o Tamanho, dois degraus (× 1,25²) acima do corpo.
    @Test
    fun title_inExtraGrande_is37point5sp() {
        val style = renderedTitleStyle(ReaderSettings(fontSize = ReaderFontSize.XLARGE))

        assertEquals(37.5f, style.fontSize.value, 0.01f)
    }
}

private const val TITLE = "Na margem sul, a luta pelos salários continua"
