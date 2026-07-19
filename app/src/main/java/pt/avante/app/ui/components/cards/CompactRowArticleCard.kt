package pt.avante.app.ui.components.cards

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
fun CompactRowArticleCard(
    article: ArticleSummary,
    presentation: ArticleCardPresentation,
    formattedTaxonomy: String?,
    isBookmarked: Boolean = false,
    onClick: () -> Unit
) {
    androidx.compose.material3.OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = ArticleCardMetrics.cardHorizontalPadding, 
                vertical = 6.dp
            ),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftDivider),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(ArticleCardMetrics.cardCornerRadius),
        elevation = androidx.compose.material3.CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if (presentation.imagePolicy != pt.avante.app.ui.presentation.ArticleImagePolicy.NO_IMAGE) {
                        ArticleThumbnail(
                            heroImageUrl = article.heroImageUrl,
                            imagePolicy = presentation.imagePolicy,
                            modifier = Modifier.size(ArticleCardMetrics.smallThumbnailSize),
                            cornerRadius = 8.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        if (formattedTaxonomy != null) {
                            Text(
                                text = formattedTaxonomy,
                                style = MaterialTheme.typography.labelSmall,
                                color = OriginalRed,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (presentation.showFooter || presentation.showFavourite) {
                    Spacer(modifier = Modifier.height(12.dp))
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
