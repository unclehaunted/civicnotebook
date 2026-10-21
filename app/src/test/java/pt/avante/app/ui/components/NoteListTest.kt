package pt.avante.app.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.NoteType

/**
 * Seam: o cartão e a lista de Notas, observados pelo que o Leitor vê e toca
 * (texto, tamanho da área tocável, callbacks). Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NoteListTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun note(id: String, title: String, body: String = "") =
        NoteEntity(
            noteId = id,
            title = title,
            bodyMarkdown = body,
            createdAt = 0L,
            updatedAt = 0L,
            noteType = NoteType.STANDALONE
        )

    @Test
    fun `mais opcoes tem area tocavel de pelo menos 48dp`() {
        composeRule.setContent {
            Box(Modifier.width(360.dp)) {
                NoteCard(note = note("n1", "Uma Nota"), onClick = {}, onDelete = {})
            }
        }

        val bounds = composeRule.onNodeWithContentDescription("Mais opções").getUnclippedBoundsInRoot()

        val width = bounds.right - bounds.left
        val height = bounds.bottom - bounds.top
        assertTrue("largura $width", width >= 48.dp)
        assertTrue("altura $height", height >= 48.dp)
    }

    private fun assertExcerptBarMatchesText(excerpt: String) {
        composeRule.setContent {
            Box(Modifier.width(360.dp)) {
                NoteCard(note = note("n1", "Uma Nota", body = "> $excerpt"), onClick = {}, onDelete = {})
            }
        }

        // O cartão é clicável e funde os filhos; o texto e a barra só existem na árvore não fundida.
        val text = composeRule.onNodeWithText(excerpt, substring = true, useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val bar = composeRule.onNodeWithTag(EXCERPT_BAR_TAG, useUnmergedTree = true).getUnclippedBoundsInRoot()

        val textHeight = text.bottom - text.top
        val barHeight = bar.bottom - bar.top
        assertEquals("barra $barHeight, texto $textHeight", textHeight.value, barHeight.value, 1f)
    }

    @Test
    fun `lista mostra as Notas e eliminar na segunda chama onDelete com o id dela`() {
        val deleted = mutableListOf<String>()
        composeRule.setContent {
            NoteList(
                notes = listOf(note("n1", "Primeira Nota"), note("n2", "Segunda Nota")),
                onOpen = {},
                onDelete = { deleted += it }
            )
        }

        composeRule.onNodeWithText("Primeira Nota").assertIsDisplayed()
        composeRule.onNodeWithText("Segunda Nota").assertIsDisplayed()

        composeRule.onAllNodesWithContentDescription("Mais opções")[1].performClick()
        composeRule.onNodeWithText("Eliminar").performClick()

        assertEquals(listOf("n2"), deleted)
    }

    @Test
    fun `barra do Excerto de uma linha tem a altura do texto`() {
        assertExcerptBarMatchesText("Curto.")
    }

    @Test
    fun `barra do Excerto de duas linhas tem a altura do texto`() {
        assertExcerptBarMatchesText(
            "Um Excerto comprido que não cabe numa só linha do cartão de Nota e passa para a segunda linha sem dúvida."
        )
    }
}
