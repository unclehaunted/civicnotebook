package pt.avante.app.viewmodel

import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.NoteType

/** O botão + do Caderno e a saída do editor, contra uma Room em memória. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NewNoteLifecycleTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var db: AppDatabase
    private lateinit var caderno: CadernoViewModel
    private lateinit var editor: NoteViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        caderno = CadernoViewModel(db)
        editor = NoteViewModel(db)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private suspend fun openInEditor(noteId: String, isNewNote: Boolean = false) {
        editor.loadNote(noteId, isNewNote)
        editor.uiState.first { it is NoteEditorUiState.Success }
    }

    private suspend fun noteCount() = db.articleDao().observeAllNotes().first().size

    @Test
    fun `o id devolvido para abrir o editor ja esta na base de dados`() = runTest(dispatcher) {
        val noteId = caderno.createNewNote()

        assertNotNull(db.articleDao().getNote(noteId))
    }

    @Test
    fun `criar e sair sem editar nao deixa Nota nenhuma`() = runTest(dispatcher) {
        val before = noteCount()

        val noteId = caderno.createNewNote()
        openInEditor(noteId, isNewNote = true)
        editor.closeNote().join()

        assertEquals(before, noteCount())
    }

    @Test
    fun `criar, escrever no corpo e sair grava a Nota com esse corpo`() = runTest(dispatcher) {
        val noteId = caderno.createNewNote()
        openInEditor(noteId, isNewNote = true)

        editor.updateBody("Uma ideia")
        editor.closeNote().join()

        assertEquals("Uma ideia", db.articleDao().getNote(noteId)?.bodyMarkdown)
    }

    @Test
    fun `abrir uma Nota antiga, apagar o texto e sair nao a apaga`() = runTest(dispatcher) {
        db.articleDao().insertNote(NoteEntity("velha", "Velha", "texto", 1_000L, 5_000L, NoteType.STANDALONE))
        openInEditor("velha")

        editor.updateTitle("")
        editor.updateBody("")
        editor.closeNote().join()

        assertNotNull(db.articleDao().getNote("velha"))
    }

    @Test
    fun `sair antes de a Nova Nota acabar de carregar tambem a descarta`() = runTest(dispatcher) {
        val before = noteCount()

        val noteId = caderno.createNewNote()
        editor.loadNote(noteId, isNewNote = true)
        editor.closeNote().join()

        assertEquals(before, noteCount())
    }

    @Test
    fun `uma Nota do leitor nunca editada que se esvazie nao se apaga`() = runTest(dispatcher) {
        db.articleDao().insertNote(NoteEntity("leitor", "Um artigo", "", 1_000L, 1_000L, NoteType.ARTICLE_NOTE))
        openInEditor("leitor")

        editor.updateTitle("")
        editor.closeNote().join()

        assertNotNull(db.articleDao().getNote("leitor"))
    }

    @Test
    fun `uma Nova Nota vazia de outra sessao nao se apaga so por ser aberta`() = runTest(dispatcher) {
        db.articleDao().insertNote(NewNote.create(noteId = "antiga", now = 1_000L))
        openInEditor("antiga")

        editor.closeNote().join()

        assertNotNull(db.articleDao().getNote("antiga"))
    }
}
