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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.avante.AvanteSite
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.SoftDivider
import pt.avante.app.theme.TextPrimary
import pt.avante.app.ui.settings.LocalOpenSettings
import pt.avante.app.ui.settings.LocalReaderSettings
import pt.avante.app.theme.Spacing

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
    val readerTheme = LocalReaderSettings.current.theme
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
        is ArticleBlock.Subheading -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(readerTheme.textColor),
                modifier = modifier.padding(top = Spacing.m, bottom = Spacing.s)
            )
        }
        is ArticleBlock.Quote -> {
            Surface(
                color = Color(readerTheme.quoteSurfaceColor),
                shape = MaterialTheme.shapes.small,
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(readerTheme.quoteBorderColor)),
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
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = OriginalRed),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    androidx.compose.material3.Text("Assinar o Avante!", color = androidx.compose.ui.graphics.Color.White, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.height(Spacing.m))
                Text(
                    text = "Tem assinatura ou a edição impressa? Introduza a palavra-passe",
                    style = MaterialTheme.typography.labelLarge,
                    color = OriginalRed,
                    modifier = Modifier.clickable { openSettings?.invoke() }
                )
            }
        }
        is ArticleBlock.Image -> {
            // Future media handling
        }
    }
}
