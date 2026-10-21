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
            tiposDeLetra = loadTiposDeLetra(),
            escrita = prefs.getString(KEY_ESCRITA, null).toEnumOrDefault(Escrita.IA_WRITER_DUO),
            fontSize = prefs.getString(KEY_FONT_SIZE, null).toEnumOrDefault(ReaderFontSize.MEDIUM),
            theme = prefs.getString(KEY_THEME, null).toEnumOrDefault(ReaderTheme.PAPER),
            showImages = prefs.getBoolean(KEY_SHOW_IMAGES, true),
            spacing = prefs.getString(KEY_SPACING, null).toEnumOrDefault(ReaderSpacing.NORMAL)
        )
    }

    fun setSpacing(spacing: ReaderSpacing) {
        prefs.edit().putString(KEY_SPACING, spacing.name).apply()
        _settings.value = _settings.value.copy(spacing = spacing)
    }

    /** Sem escolha de Tipos de letra guardada, migra a fonte única de antes da #10. */
    private fun loadTiposDeLetra(): TiposDeLetra =
        if (prefs.contains(KEY_TIPOS_DE_LETRA)) {
            prefs.getString(KEY_TIPOS_DE_LETRA, null).toEnumOrDefault(TiposDeLetra.CLASSICO)
        } else {
            TiposDeLetra.daFonteAntiga(prefs.getString(KEY_LEGACY_FONT, null))
        }

    fun setTiposDeLetra(tiposDeLetra: TiposDeLetra) {
        prefs.edit()
            .putString(KEY_TIPOS_DE_LETRA, tiposDeLetra.name)
            .remove(KEY_LEGACY_FONT)
            .apply()
        _settings.value = _settings.value.copy(tiposDeLetra = tiposDeLetra)
    }

    fun setEscrita(escrita: Escrita) {
        prefs.edit().putString(KEY_ESCRITA, escrita.name).apply()
        _settings.value = _settings.value.copy(escrita = escrita)
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

        /** A fonte única de antes da #10: só se lê para migrar para [KEY_TIPOS_DE_LETRA]. */
        private const val KEY_LEGACY_FONT = "reader_font"
        private const val KEY_TIPOS_DE_LETRA = "reader_tipos_de_letra"
        private const val KEY_ESCRITA = "reader_escrita"
        private const val KEY_FONT_SIZE = "reader_font_size"
        private const val KEY_THEME = "reader_theme"
        private const val KEY_SHOW_IMAGES = "reader_show_images"
        private const val KEY_SPACING = "reader_spacing"
    }
}
