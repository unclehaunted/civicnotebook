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
import pt.avante.app.ui.settings.LocalReaderSettings

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
                vertical = Spacing.xs
            ),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftDivider),
        shape = MaterialTheme.shapes.medium,
        elevation = androidx.compose.material3.CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(Spacing.s)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if (presentation.imagePolicy != pt.avante.app.ui.presentation.ArticleImagePolicy.NO_IMAGE &&
                        LocalReaderSettings.current.showImages) {
                        ArticleThumbnail(
                            heroImageUrl = article.heroImageUrl,
                            imagePolicy = presentation.imagePolicy,
                            modifier = Modifier.size(ArticleCardMetrics.smallThumbnailSize),
                            shape = MaterialTheme.shapes.small
                        )
                        Spacer(modifier = Modifier.width(Spacing.m))
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
                            Spacer(modifier = Modifier.height(Spacing.xxs))
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
