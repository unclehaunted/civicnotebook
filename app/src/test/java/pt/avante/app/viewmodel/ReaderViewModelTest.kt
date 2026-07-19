package pt.avante.app.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.doReturn
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.ArticleDao
import pt.avante.app.data.db.HighlightEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var viewModel: ReaderViewModel
    private val mockDao: ArticleDao = mock()
    private val mockDb: AppDatabase = mock {
        on { articleDao() } doReturn mockDao
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(mockDao.getBookmarkedArticleIds()).thenReturn(flowOf(emptyList()))
        viewModel = ReaderViewModel(mockDb)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting a highlight does not delete it`() = runTest {
        val highlight = HighlightEntity("h1", "a1", "text", null, null, 0, 0, 0, 0L)
        
        viewModel.selectHighlight(highlight)
        
        assertEquals(highlight, viewModel.selectedHighlight.value)
    }

    @Test
    fun `changing article clears transient selection and annotation state`() = runTest(UnconfinedTestDispatcher()) {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect {} }
        
        viewModel.toggleAnnotationMode()
        viewModel.selectPassage(1)
        
        val highlight = HighlightEntity("h1", "a1", "text", null, null, 0, 0, 0, 0L)
        viewModel.selectHighlight(highlight)
        
        assertEquals(ReaderMode.ANNOTATION_MODE, viewModel.readerMode.value)
        assertEquals(1, viewModel.selectedPassageIndex.value)
        assertEquals(highlight, viewModel.selectedHighlight.value)
        
        // Simulating loadArticleById
        whenever(mockDao.getCachedContent("a2")).thenReturn(null)
        whenever(mockDao.getSource("a2")).thenReturn(null)
        viewModel.loadArticleById("a2")
        
        assertEquals(ReaderMode.READ_MODE, viewModel.readerMode.value)
        assertNull(viewModel.selectedPassageIndex.value)
        assertNull(viewModel.selectedHighlight.value)
        job.cancel()
    }

    @Test
    fun `explicit deletion does delete it`() = runTest(UnconfinedTestDispatcher()) {
        val job = launch(UnconfinedTestDispatcher()) { viewModel.articleHighlights.collect {} }
        whenever(mockDao.getHighlightsForArticle(org.mockito.kotlin.any())).thenReturn(emptyList())
        viewModel.removeHighlight("h1")
        org.mockito.kotlin.verify(mockDao).deleteHighlight("h1")
        job.cancel()
    }

    @Test
    fun `hasArticleNotes recalculates when article identity changes`() = runTest(UnconfinedTestDispatcher()) {
        val bookmarksFlow = MutableStateFlow(listOf("a1"))
        whenever(mockDao.getBookmarkedArticleIds()).thenReturn(bookmarksFlow)
        
        // Re-init viewModel to pick up new flow
        viewModel = ReaderViewModel(mockDb)
        val job = launch(UnconfinedTestDispatcher()) { viewModel.hasArticleNotes.collect {} }

        viewModel.loadArticleById("a1")
        assertEquals(true, viewModel.hasArticleNotes.value)

        viewModel.loadArticleById("a2")
        assertEquals(false, viewModel.hasArticleNotes.value)
        job.cancel()
    }
}
