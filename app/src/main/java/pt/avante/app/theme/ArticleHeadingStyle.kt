package pt.avante.app.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import pt.avante.app.data.ArticleSpacing

/**
 * Título e subtítulos do artigo (#13): a face de [display] dos Tipos de letra, a negrito (o
 * ficheiro 700 desenhado, como no protótipo aprovado em #7), num degrau da escala do Tamanho
 * e com a entrelinha própria dos títulos, que não segue o Espaçamento. A UI recebe-o de
 * [TipografiaDoArtigo].
 */
internal fun articleHeadingStyle(display: FontFamily, sizeSp: Float): TextStyle =
    TextStyle(fontFamily = display, fontWeight = FontWeight.Bold)
        .naEscalaDoArtigo(sizeSp)
        .copy(lineHeight = ArticleSpacing.HEADING_LINE_HEIGHT.em)
