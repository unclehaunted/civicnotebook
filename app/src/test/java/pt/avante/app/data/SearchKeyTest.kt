package pt.avante.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchKeyTest {

    @Test
    fun `tira acentos e cedilha e passa a minusculas`() {
        assertEquals("educacao", "Educação".toSearchKey())
        assertEquals("acao", "AÇÃO".toSearchKey())
        assertEquals("agua a a a", "ÁGUA à â ã".toSearchKey())
    }

    @Test
    fun `texto sem acentos fica igual`() {
        assertEquals("habitacao e saude", "habitacao e saude".toSearchKey())
    }
}
