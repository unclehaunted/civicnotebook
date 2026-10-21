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
import pt.avante.app.data.ReaderTheme
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

    // #5: a Aparência de uma instalação nova é a Automática, a primeira das cinco.
    @Test
    fun `mostra as cinco Aparencias com Automatica escolhida`() {
        show()

        composeRule.onNodeWithText("Automática").performScrollTo().assertIsSelected()
        listOf("Papel", "Branco", "Sépia", "Escuro").forEach {
            composeRule.onNodeWithText(it).performScrollTo().assertIsNotSelected()
        }
    }

    @Test
    fun `escolher Automatica depois de Papel fica guardado`() {
        ReaderSettingsStore(composeRule.activity).setTheme(ReaderTheme.PAPER)
        show()

        composeRule.onNodeWithText("Automática").performScrollTo().performClick()

        composeRule.onNodeWithText("Automática").assertIsSelected()
        composeRule.onNodeWithText("Papel").assertIsNotSelected()
        assertEquals(ReaderTheme.AUTOMATIC, reabrir().theme)
    }

    @Test
    fun `escolher Source Sans 3 na Escrita fica guardado`() {
        show()

        composeRule.onNodeWithText("iA Writer Duo").performScrollTo().assertIsSelected()
        composeRule.onNodeWithText("Source Sans 3").performScrollTo().performClick()

        composeRule.onNodeWithText("Source Sans 3").assertIsSelected()
        assertEquals(Escrita.SOURCE_SANS, reabrir().escrita)
    }
}
