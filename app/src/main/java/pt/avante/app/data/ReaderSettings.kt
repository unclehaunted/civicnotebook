package pt.avante.app.data

import pt.avante.app.R

/**
 * Preferências de leitura do utilizador (Definições).
 * Camada de dados pura: cores como Long ARGB para não depender de Compose.
 */

enum class ReaderFont(val label: String, val fontResId: Int) {
    LIBRE_FRANKLIN("Libre Franklin", R.font.libre_franklin),
    ATKINSON_HYPERLEGIBLE("Atkinson Hyperlegible", R.font.atkinson_hyperlegible_regular),
    BITTER("Bitter", R.font.bitter_regular),
    IBM_PLEX_SANS("IBM Plex Sans", R.font.ibm_plex_sans_regular),
    SOURCE_SANS("Source Sans 4", R.font.source_sans_4_regular)
}

enum class ReaderFontSize(val label: String, val spValue: Float) {
    SMALL("Pequeno", 16f),
    MEDIUM("Médio", 18f),
    LARGE("Grande", 21f),
    XLARGE("Extra grande", 24f)
}

enum class ReaderTheme(
    val label: String,
    val backgroundColor: Long,
    val textColor: Long,
    val secondaryTextColor: Long,
    val quoteSurfaceColor: Long,
    val quoteBorderColor: Long
) {
    PAPER("Papel", 0xFFFCFBF8, 0xFF333333, 0xFF59534A, 0xFFF6F1E9, 0xFFD9D0C3),
    WHITE("Branco", 0xFFFFFFFF, 0xFF333333, 0xFF59534A, 0xFFF5F5F5, 0xFFE0E0E0),
    SEPIA("Sépia", 0xFFF4ECD8, 0xFF43382C, 0xFF6B5D4F, 0xFFEDE3CB, 0xFFD8C9A8),
    DARK("Escuro", 0xFF1B1B1B, 0xFFE3E0DB, 0xFFA8A29A, 0xFF2A2A2A, 0xFF454545)
}

data class ReaderSettings(
    val font: ReaderFont = ReaderFont.LIBRE_FRANKLIN,
    val fontSize: ReaderFontSize = ReaderFontSize.MEDIUM,
    val theme: ReaderTheme = ReaderTheme.PAPER,
    val showImages: Boolean = true
)
