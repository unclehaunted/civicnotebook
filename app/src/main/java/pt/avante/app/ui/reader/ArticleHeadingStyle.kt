package pt.avante.app.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import pt.avante.app.data.ArticleSpacing
import pt.avante.app.theme.naEscalaDoArtigo
import pt.avante.app.theme.readerFontFamily
import pt.avante.app.ui.settings.LocalReaderSettings

/**
 * Título e subtítulos do artigo (#13): a face de display dos Tipos de letra, a negrito (o
 * ficheiro 700 desenhado, como no protótipo aprovado em #7), num degrau da escala do Tamanho
 * e com a entrelinha própria dos títulos, que não segue o Espaçamento.
 */
@Composable
fun articleHeadingStyle(sizeSp: Float): TextStyle =
    TextStyle(
        fontFamily = readerFontFamily(LocalReaderSettings.current.tiposDeLetra.display),
        fontWeight = FontWeight.Bold
    )
        .naEscalaDoArtigo(sizeSp)
        .copy(lineHeight = ArticleSpacing.HEADING_LINE_HEIGHT.em)
