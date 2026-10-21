package pt.avante.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.avante.app.data.ArticleSummary
import pt.avante.app.theme.SoftDivider
import pt.avante.app.theme.TextPrimary
import pt.avante.app.theme.TextSecondary
import pt.avante.app.ui.components.ArticleSummaryCard
import pt.avante.app.ui.components.CategoryRubricPill
import pt.avante.app.ui.layout.LocalAppViewport
import pt.avante.app.ui.presentation.ArticleCardMetrics
import pt.avante.app.ui.presentation.ArticleGridSpanPolicy
import pt.avante.app.ui.presentation.ArticleImageFrequencyAnalyzer
import pt.avante.app.ui.presentation.ArticlePresentationResolver
import pt.avante.app.ui.presentation.ArticleTaxonomyFormatter
import pt.avante.app.ui.presentation.FeedViewMode
import pt.avante.app.theme.Spacing

@Composable
fun ArticleListLayout(
    articles: List<ArticleSummary>,
    bookmarkedArticles: Set<String>,
    listState: LazyListState,
    onArticleClick: (ArticleSummary) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = Spacing.l)
    ) {
        items(articles.size) { index ->
            val summary = articles[index]
            val articleId = pt.avante.app.data.ArticleIdentity.generateStableId(summary.url)
            
            ArticleSummaryCard(
                article = summary,
                viewMode = FeedViewMode.LIST,
                isBookmarked = bookmarkedArticles.contains(articleId),
                onClick = { onArticleClick(summary) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleStackPager(
    articles: List<ArticleSummary>,
    bookmarkedArticles: Set<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onArticleClick: (ArticleSummary) -> Unit
) {

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = Spacing.l),
            pageSpacing = Spacing.m
        ) { page ->
            val article = articles[page]
            val articleId = pt.avante.app.data.ArticleIdentity.generateStableId(article.url)
            val isBookmarked = bookmarkedArticles.contains(articleId)

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.86f)
                    .clickable { onArticleClick(article) },
                shape = MaterialTheme.shapes.medium,
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, SoftDivider)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column {
                        AsyncImage(
                            model = article.heroImageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .heightIn(max = 350.dp)
                        )

                        Column(Modifier.padding(Spacing.l)) {
                            CategoryRubricPill(
                                formattedTaxonomy = ArticleTaxonomyFormatter.formatPillText(
                                    categoryLabel = article.categoryLabel,
                                    subcategoryLabel = article.subcategoryLabel
                                )
                            )

                            Spacer(Modifier.height(Spacing.s))

                            Text(
                                text = article.title,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(Modifier.height(Spacing.s))

                            Text(
                                text = article.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    if (isBookmarked) {
                        pt.avante.app.ui.components.cards.NotesBookmarkOverlay(
                            modifier = Modifier
                                .align(androidx.compose.ui.Alignment.TopEnd)
                                .padding(Spacing.m)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleGridLayout(
    articles: List<ArticleSummary>,
    bookmarkedArticles: Set<String>,
    gridState: androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState,
    onArticleClick: (ArticleSummary) -> Unit
) {
    val viewport = LocalAppViewport.current
    val columns = if (viewport.isCompactWidth) {
        StaggeredGridCells.Fixed(2)
    } else {
        StaggeredGridCells.Adaptive(
            minSize = when {
                viewport.isExpandedWidth -> 260.dp
                else -> 220.dp
            }
        )
    }

    LazyVerticalStaggeredGrid(
        columns = columns,
        state = gridState,
        contentPadding = PaddingValues(bottom = Spacing.l, start = Spacing.m, end = Spacing.m, top = Spacing.m),
        verticalItemSpacing = ArticleCardMetrics.gridGap,
        horizontalArrangement = Arrangement.spacedBy(ArticleCardMetrics.gridGap),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = articles.size,
            span = { index ->
                val article = articles[index]
                val presentation = ArticlePresentationResolver.resolvePresentation(
                    article = article,
                    viewMode = FeedViewMode.GRID
                )
                when (presentation.spanPolicy) {
                    ArticleGridSpanPolicy.FULL_WIDTH -> StaggeredGridItemSpan.FullLine
                    ArticleGridSpanPolicy.SINGLE_CELL -> StaggeredGridItemSpan.SingleLane
                }
            }
        ) { index ->
            val summary = articles[index]
            val articleId = pt.avante.app.data.ArticleIdentity.generateStableId(summary.url)
            
            ArticleSummaryCard(
                article = summary,
                viewMode = FeedViewMode.GRID,
                isBookmarked = bookmarkedArticles.contains(articleId),
                onClick = { onArticleClick(summary) }
            )
        }
    }
}
