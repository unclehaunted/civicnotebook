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
import pt.avante.app.ui.components.CategoryRubricPill
import pt.avante.app.ui.presentation.ArticleCardPresentation
import pt.avante.app.ui.presentation.ArticleCardMetrics
import pt.avante.app.ui.settings.LocalReaderSettings

@Composable
fun MediumArticleCard(
    article: ArticleSummary,
    presentation: ArticleCardPresentation,
    formattedTaxonomy: String?,
    formattedAuthor: String,
    isBookmarked: Boolean = false,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ArticleCardMetrics.cardHorizontalPadding, 
                vertical = 10.dp
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SoftDivider),
        shape = RoundedCornerShape(ArticleCardMetrics.cardCornerRadius),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (presentation.imagePolicy != pt.avante.app.ui.presentation.ArticleImagePolicy.NO_IMAGE &&
                        LocalReaderSettings.current.showImages) {
                        ArticleThumbnail(
                            heroImageUrl = article.heroImageUrl,
                            imagePolicy = presentation.imagePolicy,
                            modifier = Modifier.size(ArticleCardMetrics.mediumThumbnailSize),
                            cornerRadius = 8.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        if (formattedTaxonomy != null) {
                            CategoryRubricPill(
                                formattedTaxonomy = formattedTaxonomy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            maxLines = ArticleCardMetrics.mediumTitleMaxLines,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                if (presentation.showFooter || presentation.showFavourite) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ArticleCardFooter(article = article, author = formattedAuthor, presentation = presentation)
                }
            }
            if (isBookmarked) {
                NotesBookmarkOverlay(modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd))
            }
        }
    }
}
