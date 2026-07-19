package pt.avante.app.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

import pt.avante.app.viewmodel.PesquisaViewModel

import pt.avante.app.*
import pt.avante.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesquisaScreen(
    viewModel: PesquisaViewModel, 
    homeUiState: HomeUiState,
    onArticleClick: (ArticleSummary) -> Unit,
    onNoteClick: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var searchScope by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(0) } // 0 = Edição atual, 1 = Caderno
    
    Surface(modifier = Modifier.fillMaxSize(), color = WarmPaper) {
        pt.avante.app.ui.layout.AdaptiveContent(maxWidth = 960.dp) {
            Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text("Pesquisa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmPaper,
                    titleContentColor = TextPrimary
                )
            )
            
            TabRow(
                selectedTabIndex = searchScope,
                containerColor = WarmPaper,
                contentColor = OriginalRed,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[searchScope]),
                        color = OriginalRed
                    )
                }
            ) {
                Tab(
                    selected = searchScope == 0,
                    onClick = { searchScope = 0 },
                    text = { Text("Edição atual", color = if (searchScope == 0) OriginalRed else TextSecondary) }
                )
                Tab(
                    selected = searchScope == 1,
                    onClick = { searchScope = 1 },
                    text = { Text("Caderno", color = if (searchScope == 1) OriginalRed else TextSecondary) }
                )
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(if (searchScope == 0) "Pesquisar na edição atual..." else "Pesquisar no caderno...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OriginalRed,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                    cursorColor = OriginalRed,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedPlaceholderColor = TextSecondary,
                    unfocusedPlaceholderColor = TextSecondary,
                    focusedLeadingIconColor = OriginalRed,
                    unfocusedLeadingIconColor = TextSecondary
                )
            )
            
            if (searchScope == 0) {
                // Edição atual
                val isCurrentEdition = homeUiState is HomeUiState.Success && 
                    homeUiState.feed.editionNumber == viewModel.lastKnownCurrentIssueNumber
                
                if (!isCurrentEdition) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (homeUiState is HomeUiState.Loading) "A carregar edição..." else "Pesquisa na edição atual indisponível.\nLigue-se à internet para obter a edição atual.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    val feed = (homeUiState as HomeUiState.Success).feed
                    val filteredArticles = if (searchQuery.isBlank()) {
                        emptyList()
                    } else {
                        val queryLower = searchQuery.lowercase()
                        feed.articles.filter { article ->
                            article.title.lowercase().contains(queryLower) ||
                            article.author.lowercase().contains(queryLower) ||
                            article.categoryLabel?.lowercase()?.contains(queryLower) == true ||
                            article.subcategoryLabel?.lowercase()?.contains(queryLower) == true ||
                            article.summary.lowercase().contains(queryLower)
                        }
                    }
                    
                    if (filteredArticles.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isBlank()) "Pesquise artigos na edição atual." else "Nenhum resultado encontrado na edição atual.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(32.dp)
                            )
                        }
                    } else {
                        val bookmarkedArticles by viewModel.bookmarkedArticles.collectAsState()
                        LazyColumn(contentPadding = PaddingValues(bottom = 20.dp, start = 16.dp, end = 16.dp)) {
                            items(filteredArticles) { article ->
                                Box(modifier = Modifier.padding(bottom = 16.dp)) {
                                    val presentation = pt.avante.app.ui.presentation.ArticleCardPresentation(
                                        variant = pt.avante.app.ui.presentation.ArticlePresentationVariant.GRID_COMPACT_ROW,
                                        spanPolicy = pt.avante.app.ui.presentation.ArticleGridSpanPolicy.FULL_WIDTH,
                                        imagePolicy = pt.avante.app.ui.presentation.ArticleImagePolicy.NO_IMAGE,
                                        showSummary = false,
                                        showFooter = false,
                                        showFavourite = false
                                    )
                                    val articleId = pt.avante.app.data.ArticleIdentity.generateStableId(article.url)
                                    pt.avante.app.ui.components.cards.CompactRowArticleCard(
                                        article = article,
                                        presentation = presentation,
                                        formattedTaxonomy = pt.avante.app.ui.presentation.ArticleTaxonomyFormatter.formatPillText(article.categoryLabel, article.subcategoryLabel),
                                        isBookmarked = bookmarkedArticles.contains(articleId),
                                        onClick = { onArticleClick(article) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Caderno
                if (searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isBlank()) "Pesquise no seu caderno." else "Nenhum resultado encontrado no caderno.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        items(searchResults) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note.noteId) },
                                onDelete = { /* TODO in a later phase */ }
                            )
                        }
                    }
                }
            }
        }
        }
    }
}
