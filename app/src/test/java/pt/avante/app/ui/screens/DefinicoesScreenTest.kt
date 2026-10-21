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
import pt.avante.app.data.Escrita
import pt.avante.app.data.ReaderSettingsStore
import pt.avante.app.data.TiposDeLetra
import pt.avante.app.data.auth.AvanteAuthRepository
import pt.avante.app.data.auth.SessionStore

/**
 * Seam: as Definições de Tipos de letra e Escrita (#10), pelo que o Leitor vê e toca,
 * com o store real; o que escolheu é o que reencontra ao reabrir a app.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DefinicoesScreenTest {

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

    private fun reabrir() = ReaderSettingsStore(composeRule.activity).settings.value

    @Test
    fun `mostra as quatro combinacoes de Tipos de letra com Classico escolhido`() {
        show()

        composeRule.onNodeWithText("Clássico").performScrollTo().assertIsSelected()
        listOf("Livro", "Moderno", "Legível").forEach {
            composeRule.onNodeWithText(it).performScrollTo().assertIsNotSelected()
        }
    }

    @Test
    fun `escolher Livro fica guardado`() {
        show()

        composeRule.onNodeWithText("Livro").performScrollTo().performClick()

        composeRule.onNodeWithText("Livro").assertIsSelected()
        composeRule.onNodeWithText("Clássico").assertIsNotSelected()
        assertEquals(TiposDeLetra.LIVRO, reabrir().tiposDeLetra)
    }

    @Test
    fun `escolher Source Sans 4 na Escrita fica guardado`() {
        show()

        composeRule.onNodeWithText("iA Writer Duo").performScrollTo().assertIsSelected()
        composeRule.onNodeWithText("Source Sans 4").performScrollTo().performClick()

        composeRule.onNodeWithText("Source Sans 4").assertIsSelected()
        assertEquals(Escrita.SOURCE_SANS, reabrir().escrita)
    }
}
