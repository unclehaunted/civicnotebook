package pt.avante.app.data.db

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Eliminar uma Nota e anular a eliminação, contra uma Room em memória.
 * Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
class NoteDeletionTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ArticleDao

    private val note = NoteEntity(
        noteId = "n1",
        title = "Habitação",
        bodyMarkdown = "> excerto\n\nreflexão",
        createdAt = 1_000L,
        updatedAt = 2_000L,
        noteType = NoteType.EXCERPT_NOTE
    )
    private val sources = listOf(
        NoteSourceEntity("n1", "a1", "NOTE_CREATED_FROM_ARTICLE"),
        NoteSourceEntity("n1", "a2", "REFERENCE")
    )

    @Before
    fun setUp() = runTest {
        db = inMemoryDatabase()
        dao = db.articleDao()
        dao.insertNote(note)
        sources.forEach { dao.insertNoteSource(it) }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `eliminar sem anular apaga a Nota e as suas fontes`() = runTest {
        db.deleteNoteWithSources("n1")

        assertNull(dao.getNote("n1"))
        assertTrue(dao.getNoteSources("n1").isEmpty())
    }

    @Test
    fun `eliminar e anular repoe a Nota e as suas fontes como estavam`() = runTest {
        val deleted = db.deleteNoteWithSources("n1")!!

        db.restoreNote(deleted)

        assertEquals(note, dao.getNote("n1"))
        assertEquals(sources.toSet(), dao.getNoteSources("n1").toSet())
    }

    @Test
    fun `eliminar uma Nota que nao existe nao devolve nada para anular`() = runTest {
        assertNull(db.deleteNoteWithSources("inexistente"))
    }
}
