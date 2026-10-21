package pt.avante.app.data

import pt.avante.app.R

/**
 * Preferências de leitura do utilizador (Definições).
 * Camada de dados pura: cores como Long ARGB para não depender de Compose.
 */

/**
 * As faces que os [TiposDeLetra] e a [Escrita] combinam. Cada uma tem regular, itálico e negrito
 * desenhados (#6): um ficheiro próprio ou um recurso XML que fixa a instância `wght` de uma fonte
 * variável. Nunca se sintetiza itálico nem negrito sobre o regular.
 */
enum class ReaderFont(
    val label: String,
    val regularFontResId: Int,
    val italicFontResId: Int,
    val boldFontResId: Int
) {
    LIBRE_FRANKLIN(
        "Libre Franklin",
        R.font.libre_franklin_regular,
        R.font.libre_franklin_italic,
        R.font.libre_franklin_bold
    ),
    ATKINSON_HYPERLEGIBLE(
        "Atkinson Hyperlegible",
        R.font.atkinson_hyperlegible_regular,
        R.font.atkinson_hyperlegible_italic,
        R.font.atkinson_hyperlegible_700
    ),
    BITTER("Bitter", R.font.bitter_regular, R.font.bitter_italic, R.font.bitter_700),
    SOURCE_SANS(
        "Source Sans 4",
        R.font.source_sans_4_regular,
        R.font.source_sans_4_italic,
        R.font.source_sans_4_bold
    ),
    IA_WRITER_DUO(
        "iA Writer Duo",
        R.font.ia_writer_duo_regular,
        R.font.ia_writer_duo_italic,
        R.font.ia_writer_duo_bold
    )
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
    val tiposDeLetra: TiposDeLetra = TiposDeLetra.CLASSICO,
    val escrita: Escrita = Escrita.IA_WRITER_DUO,
    val fontSize: ReaderFontSize = ReaderFontSize.MEDIUM,
    val theme: ReaderTheme = ReaderTheme.PAPER,
    val showImages: Boolean = true
)
