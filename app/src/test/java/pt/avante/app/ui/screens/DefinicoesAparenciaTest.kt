package pt.avante.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.width
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.data.ReaderSettingsStore
import pt.avante.app.data.ReaderTheme
import pt.avante.app.data.auth.AvanteAuthRepository
import pt.avante.app.data.auth.SessionStore
import pt.avante.app.ui.composicao

/**
 * Seam: as cinco amostras da Aparência nas Definições, num telemóvel de 360dp, pela largura
 * com que cada uma é desenhada. Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h800dp-xxhdpi")
class DefinicoesAparenciaTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `as cinco Aparencias tem a mesma largura de pelo menos 48dp e o rotulo numa linha`() {
        val context = composeRule.activity
        composeRule.setContent {
            DefinicoesScreen(
                settingsStore = ReaderSettingsStore(context),
                authRepository = AvanteAuthRepository(SessionStore(context)),
                onBackClick = {}
            )
        }

        val larguras = ReaderTheme.entries.map {
            composeRule.onNodeWithText(it.label).performScrollTo().getUnclippedBoundsInRoot().width.value
        }

        larguras.forEach { assertTrue("$larguras: uma amostra abaixo de 48dp", it >= 48f) }
        larguras.forEach { assertEquals("$larguras: larguras diferentes", larguras.first(), it, 1f) }
        ReaderTheme.entries.forEach {
            val rotulo = composeRule.onNodeWithText(it.label, useUnmergedTree = true).composicao()
            assertEquals("\"${it.label}\" partiu a linha", 1, rotulo.lineCount)
        }
        val rotulos = ReaderTheme.entries.map {
            composeRule.onNodeWithText(it.label, useUnmergedTree = true).getUnclippedBoundsInRoot()
        }
        rotulos.zipWithNext().forEach { (antes, depois) ->
            assertTrue("rótulos sobrepostos: $antes e $depois", antes.right <= depois.left)
        }
    }
}
