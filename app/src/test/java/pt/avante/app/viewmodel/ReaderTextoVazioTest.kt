package pt.avante.app.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import pt.avante.app.data.ArticleBlockSerializer
import pt.avante.app.data.ArticleIdentity
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.auth.AvanteHttp
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.ArticleDao
import pt.avante.app.data.db.CachedArticleContentEntity

private const val URL = "https://avante.pt/pt/2759/Editorial/185426/POR-UM-NOVO-RUMO.htm"

/** A forma da página do Editorial da edição 2759: o título, e o texto vazio. */
private const val SO_TITULO = """<html><body><section id="grid-articles"><article>
  <h2 class="balance-text">POR UM NOVO RUMO</h2>
  <div class="texto-intro balance-text"> </div>
  <div class="texto"></div>
</article></section></body></html>"""

private const val COM_TEXTO = """<html><body><section id="grid-articles"><article>
  <h2 class="balance-text">É a luta organizada</h2>
  <div class="texto"><p>A situação internacional continua marcada pelo agravamento.</p></div>
</article></section></body></html>"""

/**
 * Seam: o leitor perante um artigo que o avante.pt publicou só com o título, observado pelo
 * estado do ecrã e pelo que fica guardado. O texto pode chegar mais tarde; um artigo vazio
 * guardado ficaria vazio para sempre, porque o leitor abre primeiro o que está guardado.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReaderTextoVazioTest {

    private val dao: ArticleDao = mock()
    private val db: AppDatabase = mock { on { articleDao() } doReturn dao }
    private val transporteReal = AvanteHttp.transporte
    private var pedidos = 0
    private val artigo = ArticleSummary(title = "POR UM NOVO RUMO", author = "Editorial", url = URL, summary = "")
    private val id = ArticleIdentity.generateStableId(URL)

    private fun site(html: String) {
        AvanteHttp.transporte = { url, _ ->
            pedidos++
            Jsoup.parse(html, url)
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(dao.getBookmarkedArticleIds()).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        AvanteHttp.transporte = transporteReal
        Dispatchers.resetMain()
    }

    private suspend fun abrir(): ReaderUiState {
        val viewModel = ReaderViewModel(db)
        viewModel.loadArticle(artigo, "2759")
        return viewModel.uiState.first { it is ReaderUiState.Success || it is ReaderUiState.Error }
    }

    @Test
    fun `so com titulo diz que o texto ainda nao foi publicado e nao guarda`() = runTest {
        whenever(dao.getCachedContent(id)).thenReturn(null)
        site(SO_TITULO)

        val estado = abrir()

        assertEquals(ReaderUiState.Error(ReaderErrorReason.NO_TEXT), estado)
        verify(dao, never()).insertCachedContent(any())
    }

    @Test
    fun `um artigo vazio ja guardado volta a ser pedido`() = runTest {
        whenever(dao.getCachedContent(id)).thenReturn(
            CachedArticleContentEntity(id, ArticleBlockSerializer.serialize(emptyList()), null, 0L, false, "TEMPORARY")
        )
        site(COM_TEXTO)
        whenever(dao.getHighlightsForArticle(id)).thenReturn(emptyList())

        val estado = abrir()

        assertEquals(1, pedidos)
        assertTrue(estado is ReaderUiState.Success && estado.article.blocks.isNotEmpty())
    }

    @Test
    fun `com texto mostra e guarda`() = runTest {
        whenever(dao.getCachedContent(id)).thenReturn(null)
        whenever(dao.getHighlightsForArticle(id)).thenReturn(emptyList())
        site(COM_TEXTO)

        val estado = abrir()

        assertTrue(estado is ReaderUiState.Success && estado.article.blocks.isNotEmpty())
        verify(dao).insertCachedContent(any())
    }
}
