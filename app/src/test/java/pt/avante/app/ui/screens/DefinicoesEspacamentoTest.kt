package pt.avante.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.data.ReaderSettingsStore
import pt.avante.app.data.ReaderSpacing
import pt.avante.app.data.auth.AvanteAuthRepository
import pt.avante.app.data.auth.SessionStore

/**
 * Seam: a escolha do Espaçamento (#12) nas Definições, pelo que o Leitor vê e toca,
 * com o store real; o que escolheu é o que reencontra ao reabrir a app.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DefinicoesEspacamentoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun show() {
        val context = composeRule.activity
        composeRule.setContent {
            DefinicoesScreen(
                settingsStore = ReaderSettingsStore(context),
                authRepository = AvanteAuthRepository(SessionStore(context)),
                onBackClick = {}
            )
        }
    }

    @Test
    fun `mostra os três Espaçamentos com Normal escolhido`() {
        show()

        composeRule.onNodeWithText("Normal").performScrollTo().assertIsSelected()
        composeRule.onNodeWithText("Compacto").performScrollTo().assertIsNotSelected()
        composeRule.onNodeWithText("Arejado").performScrollTo().assertIsNotSelected()
    }

    @Test
    fun `escolher Arejado fica guardado`() {
        show()

        composeRule.onNodeWithText("Arejado").performScrollTo().performClick()

        composeRule.onNodeWithText("Arejado").assertIsSelected()
        composeRule.onNodeWithText("Normal").assertIsNotSelected()
        assertEquals(ReaderSpacing.AIRY, ReaderSettingsStore(composeRule.activity).settings.value.spacing)
    }
}
