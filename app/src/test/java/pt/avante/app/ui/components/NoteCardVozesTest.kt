package pt.avante.app.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.R
import pt.avante.app.data.ReaderSettings
import pt.avante.app.data.TiposDeLetra
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.NoteType
import pt.avante.app.theme.OriginalTheme
import pt.avante.app.ui.assertDesenhadoCom
import pt.avante.app.ui.composicao
import pt.avante.app.ui.settings.LocalReaderSettings

/**
 * Seam 2 (#14): o cartão de uma Nota no Caderno com as duas vozes. As palavras do Leitor na
 * Escrita, as do jornal nas faces dos Tipos de letra; os tamanhos ficam na escala do chrome
 * (16sp). O ficheiro que desenhou cada texto reconhece-se pelas larguras (ver LarguraDoTexto).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w600dp-h900dp-xxhdpi")
class NoteCardVozesTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun cartao(settings: ReaderSettings) {
        val nota = NoteEntity(
            noteId = "n1",
            title = TITULO,
            bodyMarkdown = "> $EXCERTO\n\n$REFLEXAO",
            createdAt = 0L,
            updatedAt = 0L,
            noteType = NoteType.EXCERPT_NOTE
        )
        composeRule.setContent {
            CompositionLocalProvider(LocalReaderSettings provides settings) {
                OriginalTheme(aparencia = settings.theme) {
                    Box(Modifier.width(560.dp)) {
                        NoteCard(note = nota, onClick = {}, onDelete = {})
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    // O cartão é clicável e funde os filhos: cada texto só tem composição na árvore não fundida.
    private fun composicaoDe(texto: String) =
        composeRule.onNodeWithText(texto, substring = true, useUnmergedTree = true).composicao()

    @Test
    fun `a reflexão do Leitor aparece na Escrita`() {
        cartao(ReaderSettings())

        composeRule.activity.assertDesenhadoCom(
            composicaoDe(REFLEXAO),
            REFLEXAO,
            tamanhoSp = 16f,
            esperado = R.font.ia_writer_duo_regular,
            fallback = R.font.libre_franklin_regular
        )
    }

    @Test
    fun `o excerto do jornal aparece no itálico desenhado da face de display`() {
        cartao(ReaderSettings(tiposDeLetra = TiposDeLetra.CLASSICO))

        // O cartão mostra o excerto entre aspas: mede-se o texto inteiro, com o kerning das aspas.
        composeRule.activity.assertDesenhadoCom(
            composicaoDe(EXCERTO),
            "\"$EXCERTO\"",
            tamanhoSp = 16f,
            esperado = R.font.bitter_italic,
            fallback = R.font.bitter_regular
        )
    }

    // A spec #5: os Tipos de letra chegam ao título da Nota, como no editor.
    @Test
    fun `em Livro o título da Nota usa o negrito do Bitter`() {
        cartao(ReaderSettings(tiposDeLetra = TiposDeLetra.LIVRO))

        composeRule.activity.assertDesenhadoCom(
            composicaoDe(TITULO),
            TITULO,
            tamanhoSp = 16f,
            esperado = R.font.bitter_700,
            fallback = R.font.libre_franklin_bold
        )
    }

    private companion object {
        const val TITULO = "Contratação colectiva"
        const val EXCERTO = "Quando a fábrica pára, todos percebem."
        const val REFLEXAO = "Guardei esta frase."
    }
}
