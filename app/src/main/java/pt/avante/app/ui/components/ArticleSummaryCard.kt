package pt.avante.app.ui.components

import androidx.compose.runtime.Composable
import pt.avante.app.data.ArticleSummary
import pt.avante.app.ui.components.cards.CompactRowArticleCard
import pt.avante.app.ui.components.cards.GridTextArticleCard
import pt.avante.app.ui.components.cards.LeadArticleCard
import pt.avante.app.ui.components.cards.MediumArticleCard
import pt.avante.app.ui.presentation.*

@Composable
fun ArticleSummaryCard(
    article: ArticleSummary,
    viewMode: FeedViewMode,
    repeatedImages: Set<String> = emptySet(),
    onClick: () -> Unit
) {
    val presentation = ArticlePresentationResolver.resolvePresentation(article, viewMode, repeatedImages)
    val taxonomyText = ArticleTaxonomyFormatter.formatPillText(article.categoryLabel, article.subcategoryLabel)
    val authorText = ArticleAuthorFormatter.formatAuthor(article.author)

    when (presentation.variant) {
        ArticlePresentationVariant.LEAD_LARGE -> {
            LeadArticleCard(
                article = article,
                presentation = presentation,
                formattedTaxonomy = taxonomyText,
                formattedAuthor = authorText,
                onClick = onClick
            )
        }
        ArticlePresentationVariant.FEATURED_MEDIUM,
        ArticlePresentationVariant.OPINION_MEDIUM -> {
            MediumArticleCard(
                article = article,
                presentation = presentation,
                formattedTaxonomy = taxonomyText,
                formattedAuthor = authorText,
                onClick = onClick
            )
        }
        ArticlePresentationVariant.STANDARD_SMALL_ROW,
        ArticlePresentationVariant.GRID_COMPACT_ROW -> {
            CompactRowArticleCard(
                article = article,
                presentation = presentation,
                formattedTaxonomy = taxonomyText,
                onClick = onClick
            )
        }
        ArticlePresentationVariant.GRID_TEXT_TILE -> {
            GridTextArticleCard(
                article = article,
                presentation = presentation,
                formattedTaxonomy = taxonomyText,
                onClick = onClick
            )
        }
        // STACK_PAGE and RELATED_TILE aren't handled by this dispatcher directly, 
        // they might be in the reader. But we add fallbacks if needed.
        else -> {
            CompactRowArticleCard(
                article = article,
                presentation = presentation,
                formattedTaxonomy = taxonomyText,
                onClick = onClick
            )
        }
    }
}
