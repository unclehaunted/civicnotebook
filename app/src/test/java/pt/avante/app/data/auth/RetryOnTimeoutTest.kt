package pt.avante.app.data.auth

import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RetryOnTimeoutTest {

    @Test
    fun timeoutOnce_retriesAndReturnsSecondResult() = runBlocking {
        var calls = 0
        val result = retryOnTimeout {
            calls++
            if (calls == 1) throw SocketTimeoutException("timeout")
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(2, calls)
    }

    @Test
    fun timeoutTwice_givesUpAfterOneRetry() {
        var calls = 0
        assertThrows(SocketTimeoutException::class.java) {
            runBlocking { retryOnTimeout { calls++; throw SocketTimeoutException("timeout") } }
        }
        assertEquals(2, calls)
    }

    @Test
    fun otherErrors_areNotRetried() {
        var calls = 0
        assertThrows(IOException::class.java) {
            runBlocking { retryOnTimeout { calls++; throw IOException("404") } }
        }
        assertEquals(1, calls)
    }
}
