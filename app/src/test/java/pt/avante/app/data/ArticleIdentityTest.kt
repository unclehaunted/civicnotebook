package pt.avante.app.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.MessageDigest

class ArticleIdentityTest {

    @Test
    fun `generateStableId matches legacy sha256 output exactly`() {
        val testUrl = "https://avante.pt/pt/2610/nacional/123456/Some-article.htm"
        
        // This is the literal algorithm that was inside HomeViewModel and ReaderViewModel
        val legacyBytes = MessageDigest.getInstance("SHA-256").digest(testUrl.toByteArray())
        val legacyId = legacyBytes.joinToString("") { "%02x".format(it) }.take(16)
        
        val newId = ArticleIdentity.generateStableId(testUrl)
        
        assertEquals("The generated ID must perfectly match the existing 16-character SHA-256 prefix", legacyId, newId)
        assertEquals(16, newId.length)
    }
}
