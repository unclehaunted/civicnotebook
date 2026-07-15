package pt.avante.app.ui.screens



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.ArticleParser
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.HomeFeed
import pt.avante.app.theme.*
import kotlinx.coroutines.launch

import androidx.room.Room
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.viewmodel.HomeViewModel
import pt.avante.app.viewmodel.HomeUiState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

// import pt.avante.app.viewmodel.LibraryViewModel

import pt.avante.app.*
import pt.avante.app.ui.components.*
import pt.avante.app.R

enum class HomeLayoutMode {
    STACK, GRID, LIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedScreen(
    viewModel: HomeViewModel,
    onArticleClick: (ArticleSummary) -> Unit, 
    onFeedLoaded: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val listState = rememberLazyListState()

    var layoutMode by rememberSaveable { mutableStateOf(HomeLayoutMode.LIST) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmPaper
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                val editionText = if (uiState is HomeUiState.Success) {
                    (uiState as HomeUiState.Success).feed.editionText
                } else ""

                AvanteMasthead(
                    onMenuClick = onMenuClick,
                    onSearchClick = onSearchClick,
                    editionLabel = editionText
                )
                
                HorizontalDivider(color = SoftDivider, thickness = 1.dp)

                if (uiState is HomeUiState.Loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OriginalRed)
                    }
                } else if (uiState is HomeUiState.Success) {
                    val feed = (uiState as HomeUiState.Success).feed
                    val articles = feed.articles
                    
                    // Layout Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        SegmentedIconButton(
                            icon = Icons.Default.ViewList,
                            selected = layoutMode == HomeLayoutMode.LIST,
                            onClick = { layoutMode = HomeLayoutMode.LIST },
                            isFirst = true
                        )
                        SegmentedIconButton(
                            icon = Icons.Default.ViewCarousel,
                            selected = layoutMode == HomeLayoutMode.STACK,
                            onClick = { layoutMode = HomeLayoutMode.STACK },
                            isFirst = false,
                            isLast = false
                        )
                        SegmentedIconButton(
                            icon = Icons.Default.GridView,
                            selected = layoutMode == HomeLayoutMode.GRID,
                            onClick = { layoutMode = HomeLayoutMode.GRID },
                            isLast = true
                        )
                    }

                    when (layoutMode) {
                        HomeLayoutMode.LIST -> ArticleListLayout(articles, listState, onArticleClick)
                        HomeLayoutMode.STACK -> ArticleStackPager(articles, onArticleClick)
                        HomeLayoutMode.GRID -> ArticleGridLayout(articles, onArticleClick)
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit, isFirst: Boolean = false, isLast: Boolean = false) {
    val shape = when {
        isFirst -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
        isLast -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }
    
    Surface(
        color = if (selected) OriginalRed else Color.Transparent,
        shape = shape,
        border = BorderStroke(1.dp, OriginalRed),
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else OriginalRed,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).size(20.dp)
        )
    }
}

@Composable
fun ArticleListLayout(
    articles: List<ArticleSummary>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onArticleClick: (ArticleSummary) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        items(articles.size) { index ->
            val summary = articles[index]
            if (index == 0) {
                LeadArticleCard(article = summary, onClick = { onArticleClick(summary) })
            } else {
                CompactArticleCard(article = summary, onClick = { onArticleClick(summary) })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleStackPager(
    articles: List<ArticleSummary>,
    onArticleClick: (ArticleSummary) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { articles.size })

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 22.dp),
            pageSpacing = 14.dp
        ) { page ->
            val article = articles[page]

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.86f)
                    .clickable { onArticleClick(article) },
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, SoftDivider)
            ) {
                Column {
                    AsyncImage(
                        model = article.heroImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(330.dp)
                    )

                    Column(Modifier.padding(22.dp)) {
                        Text(
                            text = article.section ?: "Em destaque",
                            style = MaterialTheme.typography.labelMedium,
                            color = OriginalRed
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
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
    onArticleClick: (ArticleSummary) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(articles) { article ->
            SmallGridArticleCard(article, onClick = { onArticleClick(article) })
        }
    }
}

@Composable
fun SmallGridArticleCard(article: ArticleSummary, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, SoftDivider)
    ) {
        Column {
            AsyncImage(
                model = article.heroImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = article.section ?: "Avante!",
                    style = MaterialTheme.typography.labelSmall,
                    color = OriginalRed
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

