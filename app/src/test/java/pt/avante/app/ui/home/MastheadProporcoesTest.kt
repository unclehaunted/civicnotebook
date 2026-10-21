package pt.avante.app.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.theme.OriginalTheme
import pt.avante.app.ui.components.AvanteMasthead
import pt.avante.app.ui.layout.AppViewport
import pt.avante.app.ui.layout.LocalAppViewport

private const val TAGLINE = "PROLETÁRIOS DE TODOS OS PAÍSES, UNI-VOS!"
private const val SUBTITULO = "Órgão Central do Partido Comunista Português"
private const val EDICAO = "Edição n.º 2706 · 16 de outubro de 2026"

/** Meio píxel a xxhdpi folgado: as medidas de referência vêm em terços de dp. */
private const val TOLERANCIA_DP = 0.5f

/**
 * Seam: o masthead e o cabeçalho da Home, observados pela geometria desenhada.
 * As proporções são as que o mantenedor fixou (2026-10-21): as do masthead de 19 de julho,
 * commit a8bf011 ("style: refine compact masthead hierarchy"). Os números de referência foram
 * medidos neste mesmo teste nesse commit e em a095084, de geometria igual, antes de #49/#5
 * (Robolectric, xxhdpi).
 * Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h800dp-xxhdpi")
class MastheadProporcoesTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun mostrarMasthead(compacto: Boolean) {
        composeRule.setContent {
            OriginalTheme {
                AvanteMasthead(
                    onMenuClick = {},
                    onSearchClick = {},
                    isCompact = compacto,
                    modifier = Modifier.testTag("masthead")
                )
            }
        }
    }

    private fun mostrarCabecalho(larguraDaJanela: Int) {
        composeRule.setContent {
            OriginalTheme {
                CompositionLocalProvider(
                    LocalAppViewport provides AppViewport(larguraDaJanela.dp, 800.dp)
                ) {
                    Box(Modifier.testTag("cabecalho")) {
                        HomeHeader(
                            editionText = EDICAO,
                            layoutMode = HomeLayoutMode.STACK,
                            onLayoutModeChange = {},
                            onMenuClick = {},
                            onSearchClick = {}
                        )
                    }
                }
            }
        }
    }

    private fun SemanticsNodeInteraction.limites(): DpRect = getUnclippedBoundsInRoot()

    private fun assertDp(medida: String, esperado: Float, medido: Dp) =
        assertEquals(medida, esperado, medido.value, TOLERANCIA_DP)

    /** Os três botões de disposição: clicáveis e sem descrição (os ícones do masthead têm). */
    private fun segmentos(): List<DpRect> {
        val semDescricao = SemanticsMatcher.keyNotDefined(SemanticsProperties.ContentDescription)
        val densidade = composeRule.density.density
        return composeRule.onAllNodes(hasClickAction() and semDescricao).fetchSemanticsNodes().map {
            val r = it.boundsInRoot
            DpRect((r.left / densidade).dp, (r.top / densidade).dp, (r.right / densidade).dp, (r.bottom / densidade).dp)
        }
    }

    @Test
    fun `masthead compacto tem a altura de antes`() {
        mostrarMasthead(compacto = true)

        assertDp("altura", 102.33f, composeRule.onNodeWithTag("masthead").limites().height)
    }

    @Test
    @Config(qualifiers = "w840dp-h1200dp-xxhdpi")
    fun `masthead expandido tem a altura de antes`() {
        mostrarMasthead(compacto = false)

        assertDp("altura", 118.67f, composeRule.onNodeWithTag("masthead").limites().height)
    }

    @Test
    fun `cabecalho compacto tem as proporcoes de antes`() {
        mostrarCabecalho(larguraDaJanela = 360)

        assertDp("altura do cabeçalho", 143.33f, composeRule.onNodeWithTag("cabecalho").limites().height)

        val tagline = composeRule.onNodeWithText(TAGLINE).limites()
        assertDp("topo da divisa", 4f, tagline.top)
        assertDp("largura da divisa", 214.67f, tagline.width)
        assertDp("altura da divisa", 10.67f, tagline.height)

        assertDp("topo do logótipo", 16.67f, composeRule.onNodeWithContentDescription("Avante!").limites().top)

        val subtitulo = composeRule.onNodeWithText(SUBTITULO).limites()
        assertDp("topo do subtítulo", 80.67f, subtitulo.top)
        assertDp("largura do subtítulo", 233.33f, subtitulo.width)
        assertDp("altura do subtítulo", 11.67f, subtitulo.height)

        val edicao = composeRule.onNodeWithText(EDICAO).limites()
        assertDp("início da edição", 34f, edicao.left)
        assertDp("largura da edição", 198f, edicao.width)
        assertDp("altura da edição", 11.67f, edicao.height)

        val botoes = segmentos()
        assertEquals(3, botoes.size)
        botoes.forEach {
            assertDp("largura do botão", 32f, it.width)
            assertDp("altura do botão", 24f, it.height)
        }
    }

    @Test
    @Config(qualifiers = "w840dp-h1200dp-xxhdpi")
    fun `cabecalho expandido tem as proporcoes de antes`() {
        mostrarCabecalho(larguraDaJanela = 840)

        assertDp("altura do cabeçalho", 163.67f, composeRule.onNodeWithTag("cabecalho").limites().height)

        val tagline = composeRule.onNodeWithText(TAGLINE).limites()
        assertDp("topo da divisa", 8f, tagline.top)
        assertDp("largura da divisa", 233.33f, tagline.width)
        assertDp("altura da divisa", 11.67f, tagline.height)

        assertDp("topo do logótipo", 27.67f, composeRule.onNodeWithContentDescription("Avante!").limites().top)

        val subtitulo = composeRule.onNodeWithText(SUBTITULO).limites()
        assertDp("topo do subtítulo", 87.67f, subtitulo.top)
        assertDp("largura do subtítulo", 254.33f, subtitulo.width)
        assertDp("altura do subtítulo", 13f, subtitulo.height)

        val edicao = composeRule.onNodeWithText(EDICAO).limites()
        assertDp("início da edição", 36f, edicao.left)
        assertDp("largura da edição", 217.67f, edicao.width)
        assertDp("altura da edição", 13f, edicao.height)

        val botoes = segmentos()
        assertEquals(3, botoes.size)
        botoes.forEach {
            assertDp("largura do botão", 38f, it.width)
            assertDp("altura do botão", 28f, it.height)
        }
    }
}
