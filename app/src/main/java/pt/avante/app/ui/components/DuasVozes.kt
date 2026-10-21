package pt.avante.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import pt.avante.app.theme.readerFontFamily
import pt.avante.app.ui.settings.LocalReaderSettings

/**
 * As duas vozes das Notas (#14, CODING_STANDARDS.md): as palavras do Leitor na Escrita, as do
 * jornal (título da Nota, excertos `>`) nas faces dos Tipos de letra. Faces das Definições do
 * Leitor; o tamanho e a cor ficam com quem as usa.
 */
object DuasVozes {
    /** A face das palavras do Leitor: o corpo da Nota e as suas pré-visualizações. */
    val escrita: FontFamily
        @Composable @ReadOnlyComposable get() = readerFontFamily(LocalReaderSettings.current.escrita.face)

    /** A face de display dos Tipos de letra: o título da Nota e os excertos do jornal. */
    val display: FontFamily
        @Composable @ReadOnlyComposable get() = readerFontFamily(LocalReaderSettings.current.tiposDeLetra.display)

    /** Um excerto do jornal dentro de uma Nota: o itálico desenhado da face de display. */
    val excerto: SpanStyle
        @Composable @ReadOnlyComposable get() = SpanStyle(fontFamily = display, fontStyle = FontStyle.Italic)
}

/**
 * No corpo de uma Nota em markdown, as linhas começadas por `>` (excertos do jornal) ficam em
 * [excerto]; a marca `>` fica à vista, na cor [corDaMarca]. Só muda o estilo, não o texto.
 * Data class: o campo só volta a transformar quando o estilo muda.
 */
data class ExcertosDoJornal(private val excerto: SpanStyle, private val corDaMarca: Color) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val estilizado = buildAnnotatedString {
            append(text)
            var inicio = 0
            for (linha in text.text.split('\n')) {
                if (linha.startsWith(MARCA_DE_EXCERTO)) {
                    addStyle(excerto, inicio, inicio + linha.length)
                    addStyle(SpanStyle(color = corDaMarca), inicio, inicio + MARCA_DE_EXCERTO.length)
                }
                inicio += linha.length + 1
            }
        }
        return TransformedText(estilizado, OffsetMapping.Identity)
    }

    private companion object {
        const val MARCA_DE_EXCERTO = ">"
    }
}
