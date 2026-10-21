package pt.avante.app.data.avante

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import pt.avante.app.data.auth.AvanteAuthRepository
import pt.avante.app.data.auth.AvanteHttp
import pt.avante.app.data.auth.SessionStore

private const val EDICAO = "2706"
private val SESSAO = mapOf("PHPSESSID" to "sessao-de-teste")

/**
 * Seam: os pedidos que o AvanteHttp faz, observados pelas cookies que levam.
 * Com sessão, o Índice perde o marcador «Artigo de acesso livre» (docs/avante-source.md) e todos
 * os artigos pareceriam exclusivos; por isso o Índice vai sempre sem sessão.
 * Corre na JVM com Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
class IndiceAnonimoTest {

    private val pedidos = mutableMapOf<String, Map<String, String>>()
    private val transporteReal = AvanteHttp.transporte

    @Before
    fun comSessao() {
        val store = SessionStore(ApplicationProvider.getApplicationContext())
        store.sessionCookies = SESSAO
        AvanteHttp.init(AvanteAuthRepository(store))
        AvanteHttp.transporte = { url, cookies ->
            pedidos[url] = cookies
            Jsoup.parse("<html><body></body></html>", url)
        }
    }

    @After
    fun repor() {
        AvanteHttp.transporte = transporteReal
    }

    @Test
    fun `o Indice vai sem sessao`() = runTest {
        AvanteIndexParser.fetchIndexInventory(EDICAO)

        assertEquals(emptyMap<String, String>(), pedidos.getValue(AvanteSite.indexUrl(EDICAO)))
    }

    @Test
    fun `um artigo vai com sessao`() = runTest {
        val artigo = "${AvanteSite.BASE_URL}/pt/$EDICAO/nacional/1-artigo.htm"

        AvanteHttp.get(artigo)

        assertEquals(SESSAO, pedidos.getValue(artigo))
    }
}
