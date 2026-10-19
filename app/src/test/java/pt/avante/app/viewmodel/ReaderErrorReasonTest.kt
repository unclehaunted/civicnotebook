package pt.avante.app.viewmodel

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

/** Seam: o mapeamento puro Throwable -> motivo que o ecrã do leitor mostra ao utilizador. */
class ReaderErrorReasonTest {

    @Test
    fun `sem resolver o anfitriao e sem rede`() {
        assertEquals(ReaderErrorReason.NO_NETWORK, UnknownHostException("avante.pt").toReaderErrorReason())
    }

    @Test
    fun `ligacao recusada e sem rede`() {
        assertEquals(ReaderErrorReason.NO_NETWORK, ConnectException("Failed to connect").toReaderErrorReason())
    }

    @Test
    fun `tempo esgotado e timeout e nao offline`() {
        assertEquals(ReaderErrorReason.TIMEOUT, SocketTimeoutException("timeout").toReaderErrorReason())
    }

    @Test
    fun `timeout embrulhado noutra excecao continua timeout`() {
        val wrapped = RuntimeException("falhou", SocketTimeoutException("timeout"))
        assertEquals(ReaderErrorReason.TIMEOUT, wrapped.toReaderErrorReason())
    }

    @Test
    fun `falta de rede embrulhada continua sem rede`() {
        val wrapped = IllegalStateException("falhou", UnknownHostException("avante.pt"))
        assertEquals(ReaderErrorReason.NO_NETWORK, wrapped.toReaderErrorReason())
    }

    @Test
    fun `outro erro de IO e erro do servidor`() {
        assertEquals(ReaderErrorReason.SERVER, IOException("HTTP error fetching URL. Status=503").toReaderErrorReason())
    }

    @Test
    fun `erro de parse ou qualquer outro e erro do servidor`() {
        assertEquals(ReaderErrorReason.SERVER, IllegalArgumentException("html inesperado").toReaderErrorReason())
    }

    @Test
    fun `causa ciclica nao bloqueia`() {
        val a = RuntimeException("a")
        val b = RuntimeException("b", a)
        a.initCause(b)
        assertEquals(ReaderErrorReason.SERVER, a.toReaderErrorReason())
    }
}
