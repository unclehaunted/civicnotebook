package pt.avante.app.viewmodel

import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.NoteType

/** Eliminar uma Nota nos resultados da Pesquisa, contra uma Room em memória. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PesquisaNoteDeletionTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `eliminar na Pesquisa tira a Nota dos resultados e da base de dados`() = runTest(dispatcher) {
        db.articleDao().insertNote(NoteEntity("n1", "Escola", "", 0L, 0L, NoteType.STANDALONE))
        val viewModel = PesquisaViewModel(db, mock())
        backgroundScope.launch { viewModel.noteResults.collect {} }

        viewModel.onSearchQueryChanged("escola")
        viewModel.noteResults.first { results -> results.any { it.noteId == "n1" } }

        viewModel.removeNote("n1")

        // A Room avisa noutra thread; first espera pela emissão sem a Nota.
        viewModel.noteResults.first { results -> results.none { it.noteId == "n1" } }
        assertNull(db.articleDao().getNote("n1"))
    }
}
