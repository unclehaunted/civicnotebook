package pt.avante.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
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
}

/**
 * Um estilo do artigo ou do editor de Notas num tamanho de [escalaDoArtigo], em sp.
 * Não herda a entrelinha do chrome, que encavalitava as linhas nos Tamanhos grandes:
 * a entrelinha do artigo vem do Espaçamento (#12).
 */
fun TextStyle.naEscalaDoArtigo(tamanhoSp: Float): TextStyle =
    copy(fontSize = tamanhoSp.sp, lineHeight = TextUnit.Unspecified, letterSpacing = 0.sp)

/** Rótulos em maiúsculas: o único texto com tracking positivo (#11); o resto fica a 0. */
fun TextStyle.emMaiusculas(): TextStyle = copy(letterSpacing = TRACKING_DE_MAIUSCULAS_EM.em)

/**
 * Um estilo do chrome: um degrau da escala fixa, sem tracking (minúsculas) e uma entrelinha
 * na grelha de 4dp. As faces são a identidade fixa: Bitter nos títulos, Libre Franklin no resto.
 */
private fun chrome(familia: FontFamily, peso: FontWeight, degrau: DegrauDoChrome, entrelinhaSp: Int) =
    TextStyle(
        fontFamily = familia,
        fontWeight = peso,
        fontSize = degrau.sp.sp,
        lineHeight = entrelinhaSp.sp,
        letterSpacing = 0.sp
    )

private val Titulo = chrome(Bitter, FontWeight.SemiBold, DegrauDoChrome.GRANDE, entrelinhaSp = 32)
private val Rotulo = chrome(LibreFranklin, FontWeight.Medium, DegrauDoChrome.PEQUENO, entrelinhaSp = 16)

/**
 * Os papéis Material do chrome sobre a escala de 1,25 a partir de 16 (12,8 / 16 / 20 / 25).
 * Papéis vizinhos podem partilhar um degrau: só quatro tamanhos se distinguem bem num telemóvel.
 * O artigo e o editor de Notas não usam estes tamanhos: seguem o Tamanho ([escalaDoArtigo]).
 */
val Typography = Typography(
    displayLarge = Titulo,
    displayMedium = Titulo,
    displaySmall = Titulo,
    headlineLarge = Titulo,
    headlineMedium = Titulo,
    headlineSmall = chrome(Bitter, FontWeight.SemiBold, DegrauDoChrome.MEDIO, entrelinhaSp = 28),
    titleLarge = chrome(Bitter, FontWeight.SemiBold, DegrauDoChrome.MEDIO, entrelinhaSp = 28),
    titleMedium = chrome(LibreFranklin, FontWeight.Medium, DegrauDoChrome.BASE, entrelinhaSp = 24),
    titleSmall = chrome(LibreFranklin, FontWeight.Medium, DegrauDoChrome.BASE, entrelinhaSp = 24),
    bodyLarge = chrome(LibreFranklin, FontWeight.Normal, DegrauDoChrome.BASE, entrelinhaSp = 24),
    bodyMedium = chrome(LibreFranklin, FontWeight.Normal, DegrauDoChrome.BASE, entrelinhaSp = 24),
    bodySmall = chrome(LibreFranklin, FontWeight.Normal, DegrauDoChrome.PEQUENO, entrelinhaSp = 20),
    labelLarge = Rotulo,
    labelMedium = Rotulo,
    labelSmall = Rotulo
)
