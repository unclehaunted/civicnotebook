package pt.avante.app.ui.reader

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
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
import pt.avante.app.viewmodel.ReaderErrorReason

/**
 * Seam: o ecrã de erro do leitor, observado pelo que o utilizador lê e pode tocar.
 * Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ReaderErrorContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun show(
        reason: ReaderErrorReason,
        onRetry: () -> Unit = {},
        onBackClick: () -> Unit = {}
    ) {
        composeRule.setContent {
            ReaderErrorContent(reason = reason, onRetry = onRetry, onBackClick = onBackClick)
        }
    }

    private fun assertNotShown(text: String) {
        composeRule.onAllNodes(androidx.compose.ui.test.hasText(text, substring = true))
            .assertCountEquals(0)
    }

    @Test
    fun `timeout mostra o texto de timeout e nao o de offline`() {
        show(ReaderErrorReason.TIMEOUT)

        composeRule.onNodeWithText("O servidor demorou a responder").assertIsDisplayed()
        assertNotShown("offline")
        assertNotShown("Ligue-se à Internet")
    }

    @Test
    fun `sem rede usa o texto offline`() {
        show(ReaderErrorReason.NO_NETWORK)

        composeRule.onNodeWithText("Artigo indisponível offline").assertIsDisplayed()
    }

    @Test
    fun `nao guardado explica que o artigo nao esta neste dispositivo`() {
        show(ReaderErrorReason.NOT_CACHED)

        composeRule.onNodeWithText("Artigo indisponível offline").assertIsDisplayed()
        composeRule.onNodeWithText("Este artigo ainda não foi guardado neste dispositivo.", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun `erro do servidor nao fala em offline`() {
        show(ReaderErrorReason.SERVER)

        composeRule.onNodeWithText("Não foi possível carregar o artigo").assertIsDisplayed()
        assertNotShown("offline")
    }

    @Test
    fun `sem texto diz que o avante pt so tem o titulo e nao fala em offline`() {
        show(ReaderErrorReason.NO_TEXT)

        composeRule.onNodeWithText("Texto ainda não publicado").assertIsDisplayed()
        composeRule.onNodeWithText("só o título deste artigo", substring = true).assertIsDisplayed()
        assertNotShown("offline")
    }

    @Test
    fun `tentar de novo chama o callback de repetir`() {
        var retries = 0
        show(ReaderErrorReason.TIMEOUT, onRetry = { retries++ })

        composeRule.onNodeWithText("Tentar de novo").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun `voltar chama o callback de voltar`() {
        var backs = 0
        show(ReaderErrorReason.SERVER, onBackClick = { backs++ })

        composeRule.onNodeWithText("Voltar").performClick()

        assertEquals(1, backs)
    }
}
