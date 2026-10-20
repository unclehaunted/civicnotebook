package pt.avante.app.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewNoteTest {

    private val created = NewNote.create(noteId = "n1", now = 1_000L)

    @Test
    fun `uma Nova Nota deixada como estava descarta-se`() {
        assertTrue(NewNote.isLeftEmpty(created))
    }

    @Test
    fun `uma Nova Nota com o titulo apagado e sem corpo descarta-se`() {
        assertTrue(NewNote.isLeftEmpty(created.copy(title = "", updatedAt = 2_000L)))
    }

    @Test
    fun `uma Nova Nota com corpo, mesmo curto, fica`() {
        assertFalse(NewNote.isLeftEmpty(created.copy(bodyMarkdown = "x", updatedAt = 2_000L)))
    }

    @Test
    fun `uma Nova Nota com titulo alterado fica`() {
        assertFalse(NewNote.isLeftEmpty(created.copy(title = "Habitação", updatedAt = 2_000L)))
    }
}
