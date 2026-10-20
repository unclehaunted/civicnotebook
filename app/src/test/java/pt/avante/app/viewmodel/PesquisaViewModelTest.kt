package pt.avante.app.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.CurrentIssueStore
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.ArticleDao
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.NoteType

@OptIn(ExperimentalCoroutinesApi::class)
class PesquisaViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    private val mockDao: ArticleDao = mock()
    private val mockDb: AppDatabase = mock {
        on { articleDao() } doReturn mockDao
    }
    private lateinit var viewModel: PesquisaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        whenever(mockDao.getBookmarkedArticleIds()).thenReturn(flowOf(emptyList()))
        whenever(mockDao.observeAllNotes()).thenReturn(notes)
        viewModel = PesquisaViewModel(mockDb, mock<CurrentIssueStore>())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun note(id: String, title: String, body: String = "") =
        NoteEntity(id, title, body, createdAt = 0L, updatedAt = 0L, noteType = NoteType.STANDALONE)

    private fun article(title: String) =
        ArticleSummary(title = title, author = "", url = "https://x/$title", summary = "")

    /** Mantém os resultados a ser observados, como o ecrã faz. */
    private fun TestScope.observeResults() {
        backgroundScope.launch { viewModel.noteResults.collect {} }
        backgroundScope.launch { viewModel.editionResults.collect {} }
    }

    @Test
    fun `uma Nota com Educacao no corpo aparece sem acentos e em maiusculas`() = runTest(dispatcher) {
        notes.value = listOf(note("n1", "Escola", body = "A Educação pública"), note("n2", "Outra"))
        observeResults()

        viewModel.onSearchQueryChanged("educacao")
        advanceUntilIdle()
        assertEquals(listOf("n1"), viewModel.noteResults.value.map { it.noteId })

        viewModel.onSearchQueryChanged("EDUCACAO")
        advanceUntilIdle()
        assertEquals(listOf("n1"), viewModel.noteResults.value.map { it.noteId })
    }

    @Test
    fun `um artigo da Edicao com Habitacao no titulo aparece ao pesquisar habitacao`() = runTest(dispatcher) {
        observeResults()
        viewModel.onCurrentEditionLoaded(listOf(article("Direito à Habitação"), article("Saúde")))

        viewModel.onSearchQueryChanged("habitacao")
        advanceUntilIdle()

        assertEquals(listOf("Direito à Habitação"), viewModel.editionResults.value.map { it.title })
    }

    @Test
    fun `escrever a, ab, abc seguidos deixa os resultados sempre do texto atual`() = runTest(dispatcher) {
        notes.value = listOf(note("n1", "a"), note("n2", "ab"), note("n3", "abc"))
        observeResults()
        runCurrent()

        val shownAfterEachKey = listOf("a", "ab", "abc").map { query ->
            viewModel.onSearchQueryChanged(query)
            runCurrent()
            viewModel.noteResults.value.map { it.noteId }
        }

        assertEquals(listOf(listOf("n1", "n2", "n3"), listOf("n2", "n3"), listOf("n3")), shownAfterEachKey)
    }

    @Test
    fun `texto em branco nao mostra resultados`() = runTest(dispatcher) {
        notes.value = listOf(note("n1", "Escola"))
        observeResults()

        viewModel.onSearchQueryChanged("   ")
        advanceUntilIdle()

        assertEquals(emptyList<NoteEntity>(), viewModel.noteResults.value)
    }
}
