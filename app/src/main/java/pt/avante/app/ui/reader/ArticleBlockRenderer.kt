package pt.avante.app.ui.reader

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.ArticleSpacing
import pt.avante.app.data.avante.AvanteSite
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.theme.Cores
import pt.avante.app.ui.settings.LocalOpenSettings
import pt.avante.app.ui.settings.LocalReaderSettings
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.escalaDoArtigo

@Composable
fun ArticleBlockRenderer(
    block: ArticleBlock,
    index: Int,
    isSelected: Boolean,
    isFinalCompleteTextBlock: Boolean = false,
    articleHighlights: List<HighlightEntity>,
    onHighlightClick: (HighlightEntity) -> Unit,
    onHighlightPassage: (String, Int, Int, Int) -> Unit,
    onCreateNote: (String, Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val openSettings = LocalOpenSettings.current

    // Os três tipos de bloco de texto partilham tudo menos o estilo e a margem.
    @Composable
    fun selectable(
        content: String,
        modifier: Modifier = Modifier,
        isQuote: Boolean = false,
        isBold: Boolean = false
    ) = SelectableArticleText(
        content = content,
        index = index,
        isQuote = isQuote,
        isBold = isBold,
        isFinalBlock = isFinalCompleteTextBlock,
        articleHighlights = articleHighlights,
        onHighlightClick = onHighlightClick,
        onHighlightPassage = onHighlightPassage,
        onCreateNote = onCreateNote,
        modifier = modifier
    )

    // Espaço entre blocos do Espaçamento (#12), em sp para crescer com o texto. Cada bloco
    // leva o espaço que fica depois dele; o seguinte não tem margem superior.
    val gaps = articleBlockGaps()

    when (block) {
        is ArticleBlock.Text -> selectable(block.content, modifier.padding(bottom = gaps.paragraph))
        // Margem inferior menor, para a pergunta ficar colada à resposta.
        is ArticleBlock.Question ->
            selectable(block.content, modifier.padding(bottom = gaps.afterQuestion), isBold = true)
        is ArticleBlock.Subheading -> ArticleSubheading(
            block.text,
            modifier.padding(top = gaps.extraBeforeSubheading, bottom = gaps.afterSubheading)
        )
        // Citação (#13, story 9): itálico da face de display, cor própria e recuo, sem caixa nem borda.
        is ArticleBlock.Quote -> selectable(
            block.content,
            modifier.padding(start = gaps.quoteIndent, top = gaps.extraBeforeQuote, bottom = gaps.afterQuote),
            isQuote = true
        )
        is ArticleBlock.PaywallCTA -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xl),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AvanteSite.SUBSCRIPTION_URL))
                        context.startActivity(intent)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Cores.acento),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Text(
                        "Assinar o Avante!",
                        color = Cores.sobreAcento,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.m))
                Text(
                    text = "Tem assinatura ou a edição impressa? Introduza a palavra-passe",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cores.acento,
                    modifier = Modifier.clickable { openSettings?.invoke() }
                )
            }
        }
        is ArticleBlock.Image -> {
            // Future media handling
        }
    }
}

/** O Espaçamento resolvido em medidas de layout. */
private class ArticleBlockGaps(
    val paragraph: Dp,
    val afterQuestion: Dp,
    /**
     * O bloco anterior já deixa um espaço de parágrafo; a Citação junta-lhe a diferença para
     * chegar ao espaço antes dela (veredito de #7).
     */
    val extraBeforeQuote: Dp,
    val afterQuote: Dp,
    val quoteIndent: Dp,
    /** Como na Citação: o bloco anterior já deixa um parágrafo; o subtítulo junta o resto. */
    val extraBeforeSubheading: Dp,
    val afterSubheading: Dp
)

@Composable
private fun articleBlockGaps(): ArticleBlockGaps {
    val settings = LocalReaderSettings.current
    val spacing = ArticleSpacing.resolve(settings.fontSize, settings.spacing)
    return with(LocalDensity.current) {
        ArticleBlockGaps(
            paragraph = spacing.paragraphSp.sp.toDp(),
            afterQuestion = spacing.afterQuestionSp.sp.toDp(),
            extraBeforeQuote = (spacing.beforeQuoteSp - spacing.paragraphSp).sp.toDp(),
            afterQuote = spacing.afterQuoteSp.sp.toDp(),
            quoteIndent = spacing.quoteIndentSp.sp.toDp(),
            extraBeforeSubheading = (spacing.beforeSubheadingSp - spacing.paragraphSp).sp.toDp(),
            afterSubheading = spacing.afterSubheadingSp.sp.toDp()
        )
    }
}

/** Subtítulo do artigo: face de display a negrito, um degrau acima do corpo na escala do Tamanho. */
@Composable
private fun ArticleSubheading(text: String, modifier: Modifier = Modifier) {
    val settings = LocalReaderSettings.current
    Text(
        text = text,
        style = articleHeadingStyle(escalaDoArtigo(settings.fontSize).subtitulo),
        color = Cores.textoPrimario,
        modifier = modifier
    )
}
