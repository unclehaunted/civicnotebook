package pt.avante.app.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup

enum class AuthState {
    /** Sem sessão. */
    LOGGED_OUT,
    /** Sessão ativa (ou presumida ativa, com cookies guardadas). */
    LOGGED_IN,
    /** A palavra-passe guardada foi recusada — provavelmente rodou com o novo número. */
    PASSWORD_REJECTED
}

/**
 * Autenticação no avante.pt.
 *
 * Verificado em teste manual (docs/dev-notes/2026-10-auth/ACCESS_TEST_RESULTS.md):
 * - POST /auth com campos "email" e "password"; o captcha é só JS cliente, nunca enviado.
 * - Sucesso: HTTP 302 com Location != /auth e nova PHPSESSID no Set-Cookie.
 * - Falha: HTTP 302 com Location de volta para /auth.
 * - Sessão ~6h (Max-Age do cookie).
 */
class AvanteAuthRepository(private val store: SessionStore) {

    private val _authState = MutableStateFlow(
        if (store.sessionCookies.isNotEmpty()) AuthState.LOGGED_IN else AuthState.LOGGED_OUT
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginInProgress = MutableStateFlow(false)
    val loginInProgress: StateFlow<Boolean> = _loginInProgress.asStateFlow()

    private val loginMutex = Mutex()

    /** Último login bem-sucedido (ms). Evita tempestades de re-login em fetches paralelos. */
    @Volatile
    private var lastSuccessfulLoginAt: Long = 0L

    val isLoggedIn: Boolean get() = _authState.value == AuthState.LOGGED_IN
    val cookies: Map<String, String> get() = store.sessionCookies
    val hasStoredPassword: Boolean get() = !store.weeklyPassword.isNullOrBlank()

    /**
     * Login com a palavra-passe semanal publicada na edição impressa.
     * O username é fixo ("leitor") — credencial partilhada publicada no jornal.
     */
    suspend fun loginWithWeeklyPassword(password: String): Boolean {
        return loginMutex.withLock { doLogin(password.trim(), rejectedState = AuthState.PASSWORD_REJECTED) }
    }

    /**
     * Re-login silencioso com a palavra-passe guardada.
     * Chamado pelo [AvanteHttp] quando uma página paga volta a mostrar paywall.
     * @return true se a sessão foi renovada.
     */
    suspend fun reloginSilently(): Boolean {
        val password = store.weeklyPassword ?: return false
        return loginMutex.withLock {
            // Se outro fetch paralelo já renovou a sessão há pouco, não repetir o login —
            // basta ao chamador repetir o fetch com as cookies atuais.
            if (System.currentTimeMillis() - lastSuccessfulLoginAt < RELOGIN_THROTTLE_MS) {
                return@withLock true
            }
            doLogin(password, rejectedState = AuthState.PASSWORD_REJECTED)
        }
    }

    fun logout() {
        store.clear()
        _authState.value = AuthState.LOGGED_OUT
    }

    private suspend fun doLogin(password: String, rejectedState: AuthState): Boolean = withContext(Dispatchers.IO) {
        if (password.isBlank()) return@withContext false
        _loginInProgress.value = true
        try {
            // O login exige uma sessão pré-existente: sem o PHPSESSID do GET inicial,
            // o POST é sempre rejeitado (redirect para /auth), mesmo com credenciais corretas.
            val getResponse = Jsoup.connect(AUTH_URL)
                .method(Connection.Method.GET)
                .timeout(TIMEOUT_MS)
                .execute()

            val response = Jsoup.connect(AUTH_URL)
                .cookies(getResponse.cookies())
                .data("email", WEEKLY_USERNAME)
                .data("password", password)
                .method(Connection.Method.POST)
                .followRedirects(false)
                .timeout(TIMEOUT_MS)
                .execute()

            val location = response.header("Location") ?: ""
            val success = response.statusCode() in 300..399 && !location.contains("/auth")

            if (success) {
                store.sessionCookies = response.cookies()
                store.weeklyPassword = password
                lastSuccessfulLoginAt = System.currentTimeMillis()
                _authState.value = AuthState.LOGGED_IN
                true
            } else {
                _authState.value = rejectedState
                false
            }
        } catch (e: Exception) {
            // Erro de rede: não mudar o estado de autenticação (a sessão pode continuar válida).
            false
        } finally {
            _loginInProgress.value = false
        }
    }

    companion object {
        private const val AUTH_URL = "https://avante.pt/auth"
        private const val WEEKLY_USERNAME = "leitor"
        private const val TIMEOUT_MS = 15_000
        private const val RELOGIN_THROTTLE_MS = 60_000L
    }
}
