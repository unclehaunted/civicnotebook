package pt.avante.app.data.auth

import java.net.SocketTimeoutException

/**
 * Repete [block] uma vez se der timeout. O avante.pt serve os pedidos quase em série,
 * por isso um timeout costuma ser fila momentânea e não falta de rede.
 * Outros erros propagam logo.
 */
suspend fun <T> retryOnTimeout(block: suspend () -> T): T =
    try {
        block()
    } catch (e: SocketTimeoutException) {
        block()
    }
