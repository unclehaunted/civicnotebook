package pt.avante.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pt.avante.app.R

val Bitter = FontFamily(
    Font(R.font.bitter_regular, FontWeight.Normal),
    Font(R.font.bitter_700, FontWeight.Bold),
    Font(R.font.bitter_600, FontWeight.SemiBold)
)

val LibreFranklin = FontFamily(
    Font(R.font.libre_franklin, FontWeight.Normal),
    Font(R.font.libre_franklin, FontWeight.Medium),
    Font(R.font.libre_franklin, FontWeight.Bold)
)

val IBMPlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Bold)
)

val SourceSans4 = FontFamily(
    Font(R.font.source_sans_4_regular, FontWeight.Normal),
    Font(R.font.source_sans_4_italic, FontWeight.Normal, style = androidx.compose.ui.text.font.FontStyle.Italic)
)

val AtkinsonHyperlegible = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_700, FontWeight.Bold),
    Font(R.font.atkinson_hyperlegible_italic, FontWeight.Normal, style = androidx.compose.ui.text.font.FontStyle.Italic)
)

/** Mapeia a escolha de fonte das Definições para a FontFamily Compose (usado em previews). */
fun readerFontFamily(font: pt.avante.app.data.ReaderFont): FontFamily = when (font) {
    pt.avante.app.data.ReaderFont.LIBRE_FRANKLIN -> LibreFranklin
    pt.avante.app.data.ReaderFont.ATKINSON_HYPERLEGIBLE -> AtkinsonHyperlegible
    pt.avante.app.data.ReaderFont.BITTER -> Bitter
    pt.avante.app.data.ReaderFont.IBM_PLEX_SANS -> IBMPlexSans
    pt.avante.app.data.ReaderFont.SOURCE_SANS -> SourceSans4
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
        fontFamily = IBMPlexSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = IBMPlexSans,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = IBMPlexSans,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
