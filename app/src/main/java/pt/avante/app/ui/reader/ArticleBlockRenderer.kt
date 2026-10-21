package pt.avante.app.ui.reader

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.avante.AvanteSite
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.theme.Cores
import pt.avante.app.ui.settings.LocalOpenSettings
import pt.avante.app.ui.settings.LocalReaderSettings
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.escalaDoArtigo
import pt.avante.app.theme.naEscalaDoArtigo

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

    when (block) {
        is ArticleBlock.Text -> selectable(block.content, modifier.padding(bottom = Spacing.m))
        // Mesma margem superior de um parágrafo (nenhuma; o espaço vem do bloco anterior)
        // e margem inferior menor, para a pergunta ficar colada à resposta.
        is ArticleBlock.Question -> selectable(block.content, modifier.padding(bottom = Spacing.xs), isBold = true)
        is ArticleBlock.Subheading -> ArticleSubheading(block.text, modifier)
        is ArticleBlock.Quote -> {
            Surface(
                color = Cores.superficie,
                shape = MaterialTheme.shapes.small,
                border = androidx.compose.foundation.BorderStroke(2.dp, Cores.divisor),
                modifier = modifier.fillMaxWidth().padding(vertical = Spacing.s)
            ) {
                Box(modifier = Modifier.padding(Spacing.m)) {
                    selectable(block.content, isQuote = true)
                }
            }
        }
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

/** Subtítulo do artigo: um degrau acima do corpo na escala do Tamanho. */
@Composable
private fun ArticleSubheading(text: String, modifier: Modifier = Modifier) {
    val settings = LocalReaderSettings.current
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium.naEscalaDoArtigo(escalaDoArtigo(settings.fontSize).subtitulo),
        color = Cores.textoPrimario,
        modifier = modifier.padding(top = Spacing.m, bottom = Spacing.s)
    )
}
