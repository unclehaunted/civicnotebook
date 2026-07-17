package pt.avante.app.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.avante.app.data.ArticleSummary
import pt.avante.app.theme.*
import pt.avante.app.ui.presentation.ArticleCardPresentation
import pt.avante.app.ui.presentation.ArticleCardMetrics

@Composable
fun GridTextArticleCard(
    article: ArticleSummary,
    presentation: ArticleCardPresentation,
    formattedTaxonomy: String?,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = WarmPaper),
        border = BorderStroke(1.dp, SoftDivider),
        shape = RoundedCornerShape(ArticleCardMetrics.cardCornerRadius),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (formattedTaxonomy != null) {
                Text(
                    text = formattedTaxonomy,
                    style = MaterialTheme.typography.labelSmall,
                    color = OriginalRed,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                maxLines = ArticleCardMetrics.gridTitleMaxLines,
                overflow = TextOverflow.Ellipsis
            )
            
            if (presentation.showFooter || presentation.showFavourite) {

                Spacer(modifier = Modifier.height(12.dp))
                ArticleCardFooter(
                    article = article,
                    author = pt.avante.app.ui.presentation.ArticleAuthorFormatter.formatAuthor(article.author),
                    presentation = presentation
                )
            }
        }
    }
}
