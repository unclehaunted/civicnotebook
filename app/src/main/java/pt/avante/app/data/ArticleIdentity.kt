package pt.avante.app.data

import java.security.MessageDigest

object ArticleIdentity {
    fun generateStableId(url: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16)
    }
}
