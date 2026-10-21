package pt.avante.app.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    isBookmarked: Boolean = false,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Cores.cartao),
        border = BorderStroke(1.dp, Cores.divisor),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.m)
            ) {
                if (formattedTaxonomy != null) {
                    pt.avante.app.ui.components.CategoryRubricPill(formattedTaxonomy)
                    Spacer(modifier = Modifier.height(Spacing.xs))
                }
                
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Cores.textoPrimario,
                    maxLines = ArticleCardMetrics.gridTitleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (presentation.showFooter || presentation.showFavourite) {

                    Spacer(modifier = Modifier.height(Spacing.s))
                    ArticleCardFooter(
                        article = article,
                        author = pt.avante.app.ui.presentation.ArticleAuthorFormatter.formatAuthor(article.author),
                        presentation = presentation
                    )
                }
            }
            if (isBookmarked) {
                NotesBookmarkOverlay(modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd))
            }
        }
    }
}
