package pt.avante.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Seam: o ecrã Sobre, observado pelo que o Leitor lê e pode tocar.
 * O browser é substituído por um `UriHandler` que regista o endereço aberto.
 * Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SobreScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val openedUris = mutableListOf<String>()

    private fun show(versionName: String = "1.0", onBackClick: () -> Unit = {}) {
        val uriHandler = object : UriHandler {
            override fun openUri(uri: String) {
                openedUris += uri
            }
        }
        composeRule.setContent {
            CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                SobreScreen(versionName = versionName, onBackClick = onBackClick)
            }
        }
    }

    @Test
    fun `mostra o nome da app e a versao da build`() {
        show(versionName = "2.7.1")

        composeRule.onNodeWithText("Avante!").assertIsDisplayed()
        composeRule.onNodeWithText("2.7.1", substring = true).assertIsDisplayed()
    }

    @Test
    fun `diz que e um projeto independente do Avante`() {
        show()

        composeRule.onNodeWithText(
            "Projeto independente, sem ligação oficial ao «Avante!». " +
                "Os artigos vêm de avante.pt e pertencem aos seus autores e ao «Avante!»."
        ).assertIsDisplayed()
    }

    @Test
    fun `mostra a licenca GPLv3`() {
        show()

        composeRule.onNodeWithText("GNU GPLv3", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `a ligacao ao repositorio abre o repositorio publico no browser`() {
        show()

        composeRule.onNodeWithText("github.com/unclehaunted/civicnotebook", substring = true)
            .performScrollTo()
            .performClick()

        assertEquals(listOf("https://github.com/unclehaunted/civicnotebook"), openedUris)
    }

    @Test
    fun `o botao voltar chama o callback de voltar`() {
        var backs = 0
        show(onBackClick = { backs++ })

        composeRule.onNodeWithContentDescription("Voltar").performClick()

        assertEquals(1, backs)
    }
}
