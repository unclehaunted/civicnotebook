package pt.avante.app.data

import pt.avante.app.R

/**
 * Preferências de leitura do utilizador (Definições).
 * Camada de dados pura, sem Compose.
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

/** A Aparência (glossário). As cores de cada uma resolvem-se em [paleta] (`Paleta.kt`). */
enum class ReaderTheme(val label: String) {
    PAPER("Papel"),
    WHITE("Branco"),
    SEPIA("Sépia"),
    DARK("Escuro")
}

data class ReaderSettings(
    val tiposDeLetra: TiposDeLetra = TiposDeLetra.CLASSICO,
    val escrita: Escrita = Escrita.IA_WRITER_DUO,
    val fontSize: ReaderFontSize = ReaderFontSize.MEDIUM,
    val theme: ReaderTheme = ReaderTheme.PAPER,
    val showImages: Boolean = true
)
