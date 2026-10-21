package pt.avante.app.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import pt.avante.app.ui.layout.scaffoldContentPadding
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Seam: a barra de título comum, observada pelo que o Leitor lê e pode tocar
 * e pela posição em que aparece sob a barra de estado. Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TitleBarTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `mostra o titulo`() {
        composeRule.setContent { TitleBar(title = "Caderno") }

        composeRule.onNodeWithText("Caderno").assertIsDisplayed()
    }

    @Test
    fun `sem voltar nao ha botao Voltar`() {
        composeRule.setContent { TitleBar(title = "Caderno") }

        composeRule.onAllNodes(hasContentDescription("Voltar")).assertCountEquals(0)
    }

    @Test
    fun `com voltar o botao Voltar chama o callback`() {
        var backs = 0
        composeRule.setContent { TitleBar(title = "Definições", onBack = { backs++ }) }

        composeRule.onNodeWithContentDescription("Voltar").performClick()

        assertEquals(1, backs)
    }

    @Test
    fun `com barra de estado de 24dp a barra comeca a 24dp do topo`() {
        composeRule.setContent {
            TitleBar(title = "Definições", windowInsets = statusBar24)
        }

        composeRule.onNodeWithTag(TITLE_BAR_TAG).assertTopPositionInRootIsEqualTo(24.dp)
    }

    @Test
    fun `no conteudo ja afastado pelo Scaffold a margem nao se soma duas vezes`() {
        composeRule.setContent {
            Box(Modifier.scaffoldContentPadding(PaddingValues(top = 24.dp))) {
                TitleBar(title = "Caderno", windowInsets = statusBar24)
            }
        }

        composeRule.onNodeWithTag(TITLE_BAR_TAG).assertTopPositionInRootIsEqualTo(24.dp)
    }

    private val statusBar24 = WindowInsets(top = 24.dp)
}
