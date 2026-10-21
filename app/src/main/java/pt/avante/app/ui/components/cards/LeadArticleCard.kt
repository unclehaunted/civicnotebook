package pt.avante.app.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.avante.app.data.ArticleSummary
import pt.avante.app.theme.*
import pt.avante.app.ui.components.CategoryRubricPill
import pt.avante.app.ui.presentation.ArticleCardPresentation
import pt.avante.app.ui.presentation.ArticleCardMetrics
import pt.avante.app.ui.settings.LocalReaderSettings

@Composable
fun LeadArticleCard(
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
                vertical = ArticleCardMetrics.cardVerticalPadding
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Cores.cartao),
        border = BorderStroke(1.dp, Cores.divisor),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                if (presentation.imagePolicy != pt.avante.app.ui.presentation.ArticleImagePolicy.NO_IMAGE &&
                    LocalReaderSettings.current.showImages) {
                    ArticleThumbnail(
                        heroImageUrl = article.heroImageUrl,
                        imagePolicy = presentation.imagePolicy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ArticleCardMetrics.leadImageHeight),
                        shape = RectangleShape
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.l)
                ) {
                    if (formattedTaxonomy != null) {
                        CategoryRubricPill(
                            formattedTaxonomy = formattedTaxonomy
                        )
                        Spacer(modifier = Modifier.height(Spacing.s))
                    }
                    
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Cores.textoPrimario,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (presentation.showSummary && article.summary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(Spacing.s))
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Cores.textoSecundario,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    if (presentation.showFooter || presentation.showFavourite) {
                        Spacer(modifier = Modifier.height(Spacing.m))
                        ArticleCardFooter(article = article, author = formattedAuthor, presentation = presentation)
                    }
                }
            }
            if (isBookmarked) {
                NotesBookmarkOverlay(modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd))
            }
        }
    }
}
