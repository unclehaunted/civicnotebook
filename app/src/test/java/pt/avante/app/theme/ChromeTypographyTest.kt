package pt.avante.app.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Os estilos Material do chrome (#11) estão na escala fixa de 1,25 a partir de 16,
 * nenhum abaixo de 12sp, sem tracking em minúsculas e nas faces da identidade fixa.
 */
class ChromeTypographyTest {

    private val estilos: Map<String, TextStyle> = with(Typography) {
        mapOf(
            "displayLarge" to displayLarge, "displayMedium" to displayMedium, "displaySmall" to displaySmall,
            "headlineLarge" to headlineLarge, "headlineMedium" to headlineMedium, "headlineSmall" to headlineSmall,
            "titleLarge" to titleLarge, "titleMedium" to titleMedium, "titleSmall" to titleSmall,
            "bodyLarge" to bodyLarge, "bodyMedium" to bodyMedium, "bodySmall" to bodySmall,
            "labelLarge" to labelLarge, "labelMedium" to labelMedium, "labelSmall" to labelSmall
        )
    }

    @Test
    fun cadaEstilo_temUmTamanhoDaEscalaDoChrome() {
        val degraus = DegrauDoChrome.entries.map { it.sp }
        estilos.forEach { (papel, estilo) ->
            assertTrue("$papel tem ${estilo.fontSize}", estilo.fontSize.value in degraus)
        }
    }

    @Test
    fun nenhumEstilo_ficaAbaixoDe12sp() {
        estilos.forEach { (papel, estilo) ->
            assertTrue("$papel tem ${estilo.fontSize}", estilo.fontSize.value >= 12f)
        }
    }

    @Test
    fun nenhumEstilo_temTrackingEmMinusculas() {
        estilos.forEach { (papel, estilo) ->
            assertEquals(papel, 0.sp, estilo.letterSpacing)
        }
    }

    @Test
    fun cadaEstilo_usaAsFacesDaIdentidadeFixa() {
        estilos.forEach { (papel, estilo) ->
            assertTrue("$papel usa ${estilo.fontFamily}", estilo.fontFamily in listOf(Bitter, LibreFranklin))
        }
    }
}
