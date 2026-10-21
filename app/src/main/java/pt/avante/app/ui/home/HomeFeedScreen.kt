package pt.avante.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.avante.app.data.ArticleSummary
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.WarmPaper
import pt.avante.app.ui.layout.AdaptiveContent
import pt.avante.app.viewmodel.HomeUiState
import pt.avante.app.viewmodel.HomeViewModel
import pt.avante.app.theme.Spacing

@Composable
fun HomeFeedScreen(
    viewModel: HomeViewModel,
    onArticleClick: (ArticleSummary, String) -> Unit,
    onFeedLoaded: (String) -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val layoutMode by viewModel.layoutMode.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmPaper
    ) {
        AdaptiveContent(maxWidth = 1400.dp) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    HomeHeader(
                        editionText = if (uiState is HomeUiState.Success) (uiState as HomeUiState.Success).feed.editionText else "",
                        layoutMode = layoutMode,
                        onLayoutModeChange = { viewModel.setLayoutMode(it) },
                        onMenuClick = onMenuClick,
                        onSearchClick = onSearchClick
                    )

                    if (uiState is HomeUiState.Loading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = OriginalRed)
                        }
                    } else if (uiState is HomeUiState.Error) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.Text(text = (uiState as HomeUiState.Error).message, color = androidx.compose.ui.graphics.Color.Black)
                            androidx.compose.material3.Button(onClick = { viewModel.loadFeed(true) }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Spacing.xl)) {
                                androidx.compose.material3.Text("Tentar Novamente")
                            }
                        }
                    } else if (uiState is HomeUiState.Success) {
                        val feed = (uiState as HomeUiState.Success).feed
                        val articles = feed.articles
                        val bookmarkedArticles by viewModel.bookmarkedArticles.collectAsState()

                        val listSaved = viewModel.feedPositions[HomeLayoutMode.LIST] ?: (0 to 0)
                        val listState = rememberLazyListState(initialFirstVisibleItemIndex = listSaved.first, initialFirstVisibleItemScrollOffset = listSaved.second)

                        val gridSaved = viewModel.feedPositions[HomeLayoutMode.GRID] ?: (0 to 0)
                        val gridState = rememberLazyStaggeredGridState(initialFirstVisibleItemIndex = gridSaved.first, initialFirstVisibleItemScrollOffset = gridSaved.second)

                        val stackSaved = viewModel.feedPositions[HomeLayoutMode.STACK] ?: (0 to 0)
                        val pagerState = rememberPagerState(initialPage = stackSaved.first, pageCount = { articles.size })

                        LaunchedEffect(listState) {
                            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }.collect {
                                viewModel.feedPositions[HomeLayoutMode.LIST] = it
                            }
                        }
                        LaunchedEffect(gridState) {
                            snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }.collect {
                                viewModel.feedPositions[HomeLayoutMode.GRID] = it
                            }
                        }
                        LaunchedEffect(pagerState) {
                            snapshotFlow { pagerState.currentPage }.collect {
                                viewModel.feedPositions[HomeLayoutMode.STACK] = it to 0
                            }
                        }

                        when (layoutMode) {
                            HomeLayoutMode.LIST -> ArticleListLayout(
                                articles = articles,
                                bookmarkedArticles = bookmarkedArticles,
                                listState = listState,
                                onArticleClick = { onArticleClick(it, feed.editionNumber) }
                            )
                            HomeLayoutMode.STACK -> ArticleStackPager(
                                articles = articles,
                                bookmarkedArticles = bookmarkedArticles,
                                pagerState = pagerState,
                                onArticleClick = { onArticleClick(it, feed.editionNumber) }
                            )
                            HomeLayoutMode.GRID -> ArticleGridLayout(
                                articles = articles,
                                bookmarkedArticles = bookmarkedArticles,
                                gridState = gridState,
                                onArticleClick = { onArticleClick(it, feed.editionNumber) }
                            )
                        }
                    }
                }
            }
        }
    }
}
