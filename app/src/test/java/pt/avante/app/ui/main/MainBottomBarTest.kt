package pt.avante.app.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
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
 * Seam: a barra inferior em ecrã estreito, observada pelo que o Leitor vê e toca.
 * Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainBottomBarTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `mostra os quatro separadores com rotulo sem nenhum toque previo`() {
        composeRule.setContent {
            MainBottomBar(currentTab = MainDestination.EDICAO, onTabSelected = {})
        }

        listOf("Edição", "Caderno", "Arquivo", "Pesquisa").forEach {
            composeRule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun `um toque num separador muda de ecra`() {
        val selected = mutableListOf<MainDestination>()
        composeRule.setContent {
            MainBottomBar(currentTab = MainDestination.EDICAO, onTabSelected = { selected += it })
        }

        composeRule.onNodeWithText("Arquivo").performClick()

        assertEquals(listOf(MainDestination.ARQUIVO), selected)
    }
}
