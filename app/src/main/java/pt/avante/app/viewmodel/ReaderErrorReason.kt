package pt.avante.app.viewmodel

import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Porque é que o leitor não conseguiu mostrar o artigo. Decide o texto do ecrã de erro. */
enum class ReaderErrorReason {
    /** Sem ligação à rede (DNS, ligação recusada, sem rota). */
    NO_NETWORK,

    /** A ligação existe mas o servidor não respondeu a tempo. */
    TIMEOUT,

    /** O servidor respondeu mal, ou o conteúdo não pôde ser lido (HTTP, parse, outro). */
    SERVER,

    /** O artigo não está guardado neste dispositivo (abertura a partir das notas, sem rede). */
    NOT_CACHED
}

private const val MAX_CAUSE_DEPTH = 10

/**
 * Classifica uma falha de carregamento. Percorre a cadeia de causas (a primeira causa
 * reconhecida ganha) para que exceções embrulhadas não caiam no caso genérico.
 */
fun Throwable.toReaderErrorReason(): ReaderErrorReason {
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < MAX_CAUSE_DEPTH) {
        when (current) {
            is SocketTimeoutException -> return ReaderErrorReason.TIMEOUT
            is UnknownHostException,
            is ConnectException,
            is NoRouteToHostException -> return ReaderErrorReason.NO_NETWORK
        }
        current = current.cause?.takeIf { it !== current }
        depth++
    }
    return ReaderErrorReason.SERVER
}
