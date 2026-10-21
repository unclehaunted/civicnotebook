package pt.avante.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pt.avante.app.R
import pt.avante.app.data.ReaderFont

// Itálico e negrito sempre de ficheiros desenhados ou dos recursos XML que fixam a instância
// wght de uma fonte variável (a instância por omissão é a mais leve): nunca sintetizados (#6).

val Bitter = FontFamily(
    Font(R.font.bitter_regular, FontWeight.Normal),
    Font(R.font.bitter_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.bitter_600, FontWeight.SemiBold),
    Font(R.font.bitter_700, FontWeight.Bold)
)

val LibreFranklin = FontFamily(
    Font(R.font.libre_franklin_regular, FontWeight.Normal),
    Font(R.font.libre_franklin_medium, FontWeight.Medium),
    Font(R.font.libre_franklin_bold, FontWeight.Bold),
    Font(R.font.libre_franklin_italic, FontWeight.Normal, FontStyle.Italic)
)

val SourceSans4 = FontFamily(
    Font(R.font.source_sans_4_regular, FontWeight.Normal),
    Font(R.font.source_sans_4_bold, FontWeight.Bold),
    Font(R.font.source_sans_4_italic, FontWeight.Normal, FontStyle.Italic)
)

val AtkinsonHyperlegible = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_700, FontWeight.Bold),
    Font(R.font.atkinson_hyperlegible_italic, FontWeight.Normal, FontStyle.Italic)
)

/** Escrita (#5): a face das palavras do Leitor nas Notas. */
val IAWriterDuo = FontFamily(
    Font(R.font.ia_writer_duo_regular, FontWeight.Normal),
    Font(R.font.ia_writer_duo_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.ia_writer_duo_bold, FontWeight.Bold)
)

/** Mapeia a escolha de fonte das Definições para a FontFamily Compose (usado em previews). */
fun readerFontFamily(font: ReaderFont): FontFamily = when (font) {
    ReaderFont.LIBRE_FRANKLIN -> LibreFranklin
    ReaderFont.ATKINSON_HYPERLEGIBLE -> AtkinsonHyperlegible
    ReaderFont.BITTER -> Bitter
    ReaderFont.SOURCE_SANS -> SourceSans4
    ReaderFont.IA_WRITER_DUO -> IAWriterDuo
}

val Typography = Typography(
    displayMedium = TextStyle(
        fontFamily = Bitter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Bitter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Bitter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Bitter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),
    bodySmall = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = LibreFranklin,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
