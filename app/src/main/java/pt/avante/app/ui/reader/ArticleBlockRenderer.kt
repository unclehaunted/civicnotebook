package pt.avante.app.ui.reader

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.SoftDivider
import pt.avante.app.theme.TextPrimary
import pt.avante.app.ui.settings.LocalOpenSettings
import pt.avante.app.ui.settings.LocalReaderSettings

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

    when (block) {
        is ArticleBlock.Text -> {
            SelectableArticleText(
                content = block.content,
                index = index,
                isFinalBlock = isFinalCompleteTextBlock,
                articleHighlights = articleHighlights,
                onHighlightClick = onHighlightClick,
                onHighlightPassage = onHighlightPassage,
                onCreateNote = onCreateNote,
                modifier = modifier.padding(bottom = 16.dp)
            )
        }
        is ArticleBlock.Question -> {
            // Mesma margem superior de um parágrafo (nenhuma; o espaço vem do bloco anterior)
            // e margem inferior menor, para a pergunta ficar colada à resposta.
            SelectableArticleText(
                content = block.content,
                index = index,
                isBold = true,
                isFinalBlock = isFinalCompleteTextBlock,
                articleHighlights = articleHighlights,
                onHighlightClick = onHighlightClick,
                onHighlightPassage = onHighlightPassage,
                onCreateNote = onCreateNote,
                modifier = modifier.padding(bottom = 6.dp)
            )
        }
        is ArticleBlock.Subheading -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(readerTheme.textColor),
                modifier = modifier.padding(top = 16.dp, bottom = 12.dp)
            )
        }
        is ArticleBlock.Quote -> {
            Surface(
                color = Color(readerTheme.quoteSurfaceColor),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(readerTheme.quoteBorderColor)),
                modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    SelectableArticleText(
                        content = block.content,
                        index = index,
                        isQuote = true,
                        isFinalBlock = isFinalCompleteTextBlock,
                        articleHighlights = articleHighlights,
                        onHighlightClick = onHighlightClick,
                        onHighlightPassage = onHighlightPassage,
                        onCreateNote = onCreateNote
                    )
                }
            }
        }
        is ArticleBlock.PaywallCTA -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://editorial-avante.pcp.pt/avante.html"))
                        context.startActivity(intent)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = OriginalRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    androidx.compose.material3.Text("Assinar o Avante!", color = androidx.compose.ui.graphics.Color.White, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                }
                Spacer(modifier = Modifier.height(16.dp))
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
