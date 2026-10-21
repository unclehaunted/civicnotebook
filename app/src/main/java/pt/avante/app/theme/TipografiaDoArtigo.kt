package pt.avante.app.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import pt.avante.app.data.ArticleSpacing
import pt.avante.app.data.ReaderSettings
import pt.avante.app.ui.settings.LocalReaderSettings

/**
 * A tipografia do artigo e do editor de Notas resolvida para as Definições do Leitor: a escala
 * do Tamanho (#11), o Espaçamento (#12) e as faces dos Tipos de letra e da Escrita (#13, #14).
 * É a única entrada da UI para estes valores; obtém-se com [tipografiaDoArtigo].
 */
@Immutable
class TipografiaDoArtigo(settings: ReaderSettings) {
    private val tipos = settings.tiposDeLetra

    val escala: EscalaDoArtigo = escalaDoArtigo(settings.fontSize)
    val espacamento: ArticleSpacing = ArticleSpacing.resolve(settings.fontSize, settings.spacing)

    /** A face de display dos Tipos de letra: título, subtítulos, Citações, título da Nota. */
    val display: FontFamily = readerFontFamily(tipos.display)

    /** A face das palavras do Leitor nas Notas. */
    val escrita: FontFamily = readerFontFamily(settings.escrita.face)

    /** Um excerto do jornal dentro de uma Nota: o itálico desenhado da face de display. */
    val excerto: SpanStyle = SpanStyle(fontFamily = display, fontStyle = FontStyle.Italic)

    /** O corpo do artigo, com a entrelinha do Espaçamento. */
    val corpo: TextStyle = textoDoArtigo(readerFontFamily(tipos.texto))

    /** O corpo de uma Nota: as palavras do Leitor na Escrita, no corpo e entrelinha do artigo. */
    val corpoDaNota: TextStyle = textoDoArtigo(escrita)

    val titulo: TextStyle = articleHeadingStyle(display, escala.titulo)
    val subtitulo: TextStyle = articleHeadingStyle(display, escala.subtitulo)
    val tituloDaNota: TextStyle = articleHeadingStyle(display, escala.tituloDaNota)

    /** O ficheiro de fonte de [papel], para o TextView do artigo. */
    fun faceResId(papel: PapelDoTexto): Int = papel.faceResId(tipos)

    /** O tamanho de [papel], em sp. */
    fun tamanhoSp(papel: PapelDoTexto): Float = papel.tamanhoSp(escala)

    /**
     * Texto do artigo na [face]: o corpo do Tamanho e a entrelinha do Espaçamento, em múltiplos
     * do tamanho do texto (não da altura natural da fonte, que varia de face para face).
     */
    private fun textoDoArtigo(face: FontFamily): TextStyle =
        TextStyle(fontFamily = face, fontWeight = FontWeight.Normal)
            .naEscalaDoArtigo(escala.corpo)
            .copy(lineHeight = espacamento.lineHeight.em)
}

/**
 * A [TipografiaDoArtigo] das [settings] (por omissão as do Leitor, de [LocalReaderSettings]),
 * calculada uma vez enquanto as Definições não mudam.
 */
@Composable
fun tipografiaDoArtigo(settings: ReaderSettings = LocalReaderSettings.current): TipografiaDoArtigo =
    remember(settings) { TipografiaDoArtigo(settings) }
