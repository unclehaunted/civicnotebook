package pt.avante.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persistência das preferências de leitura (SharedPreferences + StateFlow reativo).
 * Segue o padrão de [CurrentIssueStore].
 */
class ReaderSettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<ReaderSettings> = _settings.asStateFlow()

    private fun load(): ReaderSettings {
        return ReaderSettings(
            font = prefs.getString(KEY_FONT, null).toEnumOrDefault(ReaderFont.LIBRE_FRANKLIN),
            fontSize = prefs.getString(KEY_FONT_SIZE, null).toEnumOrDefault(ReaderFontSize.MEDIUM),
            theme = prefs.getString(KEY_THEME, null).toEnumOrDefault(ReaderTheme.PAPER),
            showImages = prefs.getBoolean(KEY_SHOW_IMAGES, true)
        )
    }

    fun setFont(font: ReaderFont) {
        prefs.edit().putString(KEY_FONT, font.name).apply()
        _settings.value = _settings.value.copy(font = font)
    }

    fun setFontSize(fontSize: ReaderFontSize) {
        prefs.edit().putString(KEY_FONT_SIZE, fontSize.name).apply()
        _settings.value = _settings.value.copy(fontSize = fontSize)
    }

    fun setTheme(theme: ReaderTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun setShowImages(showImages: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_IMAGES, showImages).apply()
        _settings.value = _settings.value.copy(showImages = showImages)
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T {
        if (this == null) return default
        return runCatching { enumValueOf<T>(this) }.getOrDefault(default)
    }

    companion object {
        private const val PREFS_NAME = "pt.avante.app.reader_settings"
        private const val KEY_FONT = "reader_font"
        private const val KEY_FONT_SIZE = "reader_font_size"
        private const val KEY_THEME = "reader_theme"
        private const val KEY_SHOW_IMAGES = "reader_show_images"
    }
}
