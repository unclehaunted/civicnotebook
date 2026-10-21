package pt.avante.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.R
import pt.avante.app.data.Escrita
import pt.avante.app.data.ReaderFontSize
import pt.avante.app.data.ReaderSettings
import pt.avante.app.data.ReaderSpacing
import pt.avante.app.data.TiposDeLetra
import pt.avante.app.theme.OriginalTheme
import pt.avante.app.ui.assertDesenhadoCom
import pt.avante.app.ui.composicao
import pt.avante.app.ui.settings.LocalReaderSettings

/**
 * Seam 2 (#14): o editor de Nota com as duas vozes, observado pelo que o Leitor vê. O ficheiro
 * de fonte que desenhou cada texto reconhece-se pelas larguras dos glifos (ver LarguraDoTexto).
 * Densidade xxhdpi para as diferenças de largura serem mensuráveis.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w600dp-h900dp-xxhdpi")
class NoteEditorVozesTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun editor(settings: ReaderSettings, titulo: String = TITULO, corpo: String = CORPO) {
        composeRule.setContent {
            CompositionLocalProvider(LocalReaderSettings provides settings) {
                OriginalTheme(aparencia = settings.theme) {
                    NoteEditorFields(title = titulo, body = corpo, onTitleChange = {}, onBodyChange = {})
                }
            }
        }
        composeRule.waitForIdle()
    }

    // Médio: corpo 18; o título da Nota fica um degrau abaixo do título de artigo, 18 × 1,25.
    @Test
    fun `em Moderno o título da Nota usa o negrito do Libre Franklin`() {
        editor(ReaderSettings(tiposDeLetra = TiposDeLetra.MODERNO))

        composeRule.activity.assertDesenhadoCom(
            composeRule.onNodeWithText(TITULO).composicao(),
            TITULO,
            tamanhoSp = 22.5f,
            esperado = R.font.libre_franklin_bold,
            fallback = R.font.bitter_700
        )
    }

    @Test
    fun `o corpo da Nota usa a Escrita escolhida, iA Writer Duo`() {
        editor(ReaderSettings(escrita = Escrita.IA_WRITER_DUO))

        composeRule.activity.assertDesenhadoCom(
            composeRule.onNodeWithText(CORPO).composicao(),
            CORPO,
            tamanhoSp = 18f,
            esperado = R.font.ia_writer_duo_regular,
            fallback = R.font.libre_franklin_regular
        )
    }

    @Test
    fun `o corpo da Nota usa a Escrita escolhida, Source Sans 4`() {
        editor(ReaderSettings(escrita = Escrita.SOURCE_SANS))

        composeRule.activity.assertDesenhadoCom(
            composeRule.onNodeWithText(CORPO).composicao(),
            CORPO,
            tamanhoSp = 18f,
            esperado = R.font.source_sans_4_regular,
            fallback = R.font.ia_writer_duo_regular
        )
    }

    // A marca `>` fica à vista e no itálico, como no protótipo aprovado em #7.
    @Test
    fun `um excerto do jornal fica no itálico desenhado da face de display`() {
        editor(ReaderSettings(tiposDeLetra = TiposDeLetra.CLASSICO), corpo = "$EXCERTO\n$CORPO")

        val composicao = composeRule.onNodeWithText(CORPO, substring = true).composicao()
        composeRule.activity.assertDesenhadoCom(
            composicao,
            EXCERTO,
            tamanhoSp = 18f,
            esperado = R.font.bitter_italic,
            fallback = R.font.bitter_regular
        )
        // A linha seguinte volta às palavras do Leitor.
        composeRule.activity.assertDesenhadoCom(
            composicao,
            CORPO,
            tamanhoSp = 18f,
            esperado = R.font.ia_writer_duo_regular,
            fallback = R.font.bitter_italic
        )
    }

    @Test
    fun `em Legível o excerto usa o itálico desenhado do Atkinson`() {
        editor(ReaderSettings(tiposDeLetra = TiposDeLetra.LEGIVEL), corpo = "$CORPO\n$EXCERTO")

        composeRule.activity.assertDesenhadoCom(
            composeRule.onNodeWithText(CORPO, substring = true).composicao(),
            EXCERTO,
            tamanhoSp = 18f,
            esperado = R.font.atkinson_hyperlegible_italic,
            fallback = R.font.atkinson_hyperlegible_regular
        )
    }

    // Ao começar a escrever, a face não muda: o convite já está na Escrita.
    @Test
    fun `com o corpo vazio o convite a escrever está na Escrita`() {
        editor(ReaderSettings(), corpo = "")

        composeRule.activity.assertDesenhadoCom(
            composeRule.onNodeWithText(CONVITE, substring = true, useUnmergedTree = true).composicao(),
            CONVITE,
            tamanhoSp = 18f,
            esperado = R.font.ia_writer_duo_regular,
            fallback = R.font.libre_franklin_regular
        )
    }

    private fun spToPx(sp: Float): Float = sp * composeRule.activity.resources.displayMetrics.scaledDensity

    /** Distância, em px, entre as linhas de base das duas primeiras linhas do texto do nó. */
    private fun entrelinha(texto: String): Float {
        val composicao = composeRule.onNodeWithText(texto, substring = true).composicao()
        return composicao.getLineBaseline(1) - composicao.getLineBaseline(0)
    }

    // O editor segue o Tamanho e o Espaçamento, como o artigo: 16 × 1,65 em Pequeno Arejado.
    @Test
    fun `em Pequeno Arejado as linhas do corpo ficam a 1,65 vezes o corpo`() {
        editor(
            ReaderSettings(fontSize = ReaderFontSize.SMALL, spacing = ReaderSpacing.AIRY),
            corpo = "$CORPO\n$CORPO"
        )

        assertEquals(spToPx(26.4f), entrelinha(CORPO), 1f)
    }

    // Extra grande Compacto: 24 × (1,35 − 0,05).
    @Test
    fun `em Extra grande Compacto as linhas do corpo ficam a 1,3 vezes o corpo`() {
        editor(
            ReaderSettings(fontSize = ReaderFontSize.XLARGE, spacing = ReaderSpacing.COMPACT),
            corpo = "$CORPO\n$CORPO"
        )

        assertEquals(spToPx(31.2f), entrelinha(CORPO), 1f)
    }

    private fun dpToPx(dp: Float): Float = dp * composeRule.activity.resources.displayMetrics.density

    // Como no artigo: num tablet o texto para nos 33 em do corpo (cerca de 66 caracteres).
    @Test
    @Config(qualifiers = "w1200dp-h800dp-xxhdpi")
    fun `num ecrã largo o texto da Nota mede 33 vezes o corpo`() {
        editor(ReaderSettings())

        assertEquals(dpToPx(594f), composeRule.onNodeWithText(CORPO).composicao().size.width.toFloat(), 2f)
        assertEquals(dpToPx(594f), composeRule.onNodeWithText(TITULO).composicao().size.width.toFloat(), 2f)
    }

    private companion object {
        const val CONVITE = "Comece a escrever"
        const val EXCERTO ="> Quando a fábrica pára, todos percebem."
        const val TITULO = "Contratação colectiva"
        const val CORPO = "Guardei esta frase."
    }
}
