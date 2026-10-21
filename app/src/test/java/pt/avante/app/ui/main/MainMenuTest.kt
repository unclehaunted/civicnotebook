package pt.avante.app.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Seam: o conteúdo do Menu (folha aberta a partir da Edição), observado pelo que o Leitor vê e toca.
 * Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainMenuTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun show(onAbout: () -> Unit = {}) {
        composeRule.setContent {
            MainMenu(onSubscription = {}, onSettings = {}, onAbout = onAbout)
        }
    }

    @Test
    fun `nao tem Exportar notas`() {
        show()

        composeRule.onAllNodes(hasText("Exportar", substring = true)).assertCountEquals(0)
    }

    @Test
    fun `Sobre abre o ecra Sobre`() {
        var abouts = 0
        show(onAbout = { abouts++ })

        composeRule.onNodeWithText("Sobre").performClick()

        assertEquals(1, abouts)
    }
}
