package pt.avante.app.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pt.avante.app.data.avante.AvanteSite

/**
 * Ponto central de fetching HTTP para o avante.pt.
 *
 * Aplica as cookies de sessão a todos os pedidos e trata do re-login
 * silencioso: se uma página paga voltar a mostrar o marcador de paywall
 * (sessão de ~6h expirada), tenta renovar a sessão uma vez com a
 * palavra-passe guardada antes de desistir.
 *
 * Inicializar uma vez no arranque da app ([init]).
 */
object AvanteHttp {

    private const val TIMEOUT_MS = 15_000

    @Volatile
    private var auth: AvanteAuthRepository? = null

    fun init(repository: AvanteAuthRepository) {
        auth = repository
    }

    val isLoggedIn: Boolean get() = auth?.isLoggedIn == true

    suspend fun get(url: String): Document {
        val repository = auth
        var doc = fetch(url, repository?.cookies.orEmpty())

        if (repository != null && repository.hasStoredPassword && doc.html().contains(AvanteSite.PAYWALL_MARKER)) {
            if (repository.reloginSilently()) {
                doc = fetch(url, repository.cookies)
            }
        }
        return doc
    }

    /**
     * Um pedido sem sessão, para as páginas que só dizem a verdade a um leitor anónimo: com sessão,
     * o Índice perde o marcador «Artigo de acesso livre» (docs/avante-source.md).
     */
    suspend fun getAnonymous(url: String): Document = fetch(url, emptyMap())

    /** O pedido em si; os testes trocam-no para ver que cookies cada página leva. */
    internal var transporte: suspend (url: String, cookies: Map<String, String>) -> Document = { url, cookies ->
        withContext(Dispatchers.IO) {
            val connection = Jsoup.connect(url).timeout(TIMEOUT_MS)
            if (cookies.isNotEmpty()) connection.cookies(cookies)
            connection.get()
        }
    }

    private suspend fun fetch(url: String, cookies: Map<String, String>): Document = transporte(url, cookies)
}
