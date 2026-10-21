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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.ArticleSpacing
import pt.avante.app.data.avante.AvanteSite
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.theme.Cores
import pt.avante.app.theme.PapelDoTexto
import pt.avante.app.ui.settings.LocalOpenSettings
import pt.avante.app.ui.settings.LocalReaderSettings
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.escalaDoArtigo

@Composable
fun ArticleBlockRenderer(
    block: ArticleBlock,
    /** O bloco que se segue, se houver: o espaço entre os dois depende do par. */
    next: ArticleBlock? = null,
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
        papel: PapelDoTexto,
        modifier: Modifier = Modifier
    ) = SelectableArticleText(
        content = content,
        index = index,
        papel = papel,
        isFinalBlock = isFinalCompleteTextBlock,
        articleHighlights = articleHighlights,
        onHighlightClick = onHighlightClick,
        onHighlightPassage = onHighlightPassage,
        onCreateNote = onCreateNote,
        modifier = modifier
    )

    // Espaço entre blocos do Espaçamento (#12), em sp para crescer com o texto. Cada bloco
    // leva por baixo o espaço até ao seguinte, que depende do par (ArticleSpacing.gapSp).
    val settings = LocalReaderSettings.current
    val spacing = ArticleSpacing.resolve(settings.fontSize, settings.spacing)
    val (gapBelow, quoteIndent) = with(LocalDensity.current) {
        spacing.gapSp(block, next).sp.toDp() to spacing.quoteIndentSp.sp.toDp()
    }
    val withGap = modifier.padding(bottom = gapBelow)

    when (block) {
        is ArticleBlock.Text -> selectable(block.content, PapelDoTexto.CORPO, withGap)
        is ArticleBlock.Question -> selectable(block.content, PapelDoTexto.PERGUNTA, withGap)
        is ArticleBlock.Subheading -> ArticleSubheading(block.text, withGap)
        // Citação (#13, story 9): itálico da face de display, cor própria e recuo, sem caixa nem borda.
        is ArticleBlock.Quote -> selectable(block.content, PapelDoTexto.CITACAO, withGap.padding(start = quoteIndent))
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
