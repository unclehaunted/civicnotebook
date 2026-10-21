package pt.avante.app.ui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.avante.app.data.ArticleSummary
import androidx.compose.ui.draw.clip
import pt.avante.app.theme.*
import pt.avante.app.ui.presentation.ArticleCardPresentation
import pt.avante.app.ui.presentation.ArticleCardMetrics
import pt.avante.app.ui.settings.LocalReaderSettings

@Composable
fun GridImageArticleCard(
    article: ArticleSummary,
    presentation: ArticleCardPresentation,
    formattedTaxonomy: String?,
    isBookmarked: Boolean = false,
    onClick: () -> Unit
) {
    var imageFailed by androidx.compose.runtime.remember(article.heroImageUrl) {
        androidx.compose.runtime.mutableStateOf(false)
    }

    if (imageFailed || !LocalReaderSettings.current.showImages) {
        GridTextArticleCard(
            article = article.copy(heroImageUrl = null),
            presentation = presentation.copy(
                variant = pt.avante.app.ui.presentation.ArticlePresentationVariant.GRID_TEXT_TILE,
                imagePolicy = pt.avante.app.ui.presentation.ArticleImagePolicy.NO_IMAGE,
                showSummary = false,
                showFooter = true,
                showFavourite = true
            ),
            formattedTaxonomy = formattedTaxonomy,
            isBookmarked = isBookmarked,
            onClick = onClick
        )
        return
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = WarmPaper),
        border = BorderStroke(1.dp, SoftDivider),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xs)
                ) {
                    AsyncImage(
                        model = article.heroImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        onError = { imageFailed = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                            .clip(MaterialTheme.shapes.small)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.m)
                        .padding(bottom = Spacing.m)
                ) {
                    if (formattedTaxonomy != null) {
                        pt.avante.app.ui.components.CategoryRubricPill(formattedTaxonomy)
                        Spacer(modifier = Modifier.height(Spacing.xs))
                    }
                    
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
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
            }
            if (isBookmarked) {
                NotesBookmarkOverlay(modifier = Modifier.align(androidx.compose.ui.Alignment.TopEnd))
            }
        }
    }
}
