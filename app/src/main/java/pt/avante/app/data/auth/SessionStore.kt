package pt.avante.app.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Persistência da sessão autenticada do Avante.
 *
 * Guarda a palavra-passe semanal (publicada na edição impressa) e as cookies
 * de sessão, para permitir re-login silencioso quando a sessão expira.
 * Usa EncryptedSharedPreferences com fallback para SharedPreferences normais
 * em dispositivos com keystore problemático.
 */
class SessionStore(context: Context) {

    private val prefs: SharedPreferences = createPrefs(context)

    var weeklyPassword: String?
        get() = prefs.getString(KEY_WEEKLY_PASSWORD, null)
        set(value) {
            if (value == null) prefs.edit().remove(KEY_WEEKLY_PASSWORD).apply()
            else prefs.edit().putString(KEY_WEEKLY_PASSWORD, value).apply()
        }

    var sessionCookies: Map<String, String>
        get() {
            val raw = prefs.getString(KEY_COOKIES, null) ?: return emptyMap()
            return raw.split(";")
                .mapNotNull {
                    val idx = it.indexOf('=')
                    if (idx <= 0) null else it.substring(0, idx) to it.substring(idx + 1)
                }
                .toMap()
        }
        set(value) {
            if (value.isEmpty()) {
                prefs.edit().remove(KEY_COOKIES).apply()
            } else {
                val raw = value.entries.joinToString(";") { "${it.key}=${it.value}" }
                prefs.edit().putString(KEY_COOKIES, raw).apply()
            }
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "pt.avante.app.session"
        private const val PREFS_FALLBACK = "pt.avante.app.session_fallback"
        private const val KEY_WEEKLY_PASSWORD = "weekly_password"
        private const val KEY_COOKIES = "session_cookies"

        private fun createPrefs(context: Context): SharedPreferences {
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                context.getSharedPreferences(PREFS_FALLBACK, Context.MODE_PRIVATE)
            }
        }
    }
}
