package pt.avante.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.avante.app.data.ArticleSummary
import pt.avante.app.theme.Cores
import pt.avante.app.ui.components.NoteList
import pt.avante.app.ui.components.TitleBar
import pt.avante.app.ui.components.rememberDeleteNoteWithUndo
import pt.avante.app.viewmodel.HomeUiState
import pt.avante.app.viewmodel.PesquisaViewModel
import pt.avante.app.theme.Spacing

/** Onde a Pesquisa procura; a ordem é a dos separadores. */
private enum class SearchScope(val label: String, val placeholder: String) {
    CURRENT_EDITION("Edição atual", "Pesquisar na edição atual..."),
    CADERNO("Caderno", "Pesquisar no caderno...")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesquisaScreen(
    viewModel: PesquisaViewModel,
    homeUiState: HomeUiState,
    onArticleClick: (ArticleSummary) -> Unit,
    onNoteClick: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val noteResults by viewModel.noteResults.collectAsState()
    val editionResults by viewModel.editionResults.collectAsState()
    var searchScope by rememberSaveable { mutableStateOf(SearchScope.CURRENT_EDITION) }
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteNote = rememberDeleteNoteWithUndo(snackbarHostState, viewModel::removeNote, viewModel::restoreNote)

    Surface(modifier = Modifier.fillMaxSize(), color = Cores.fundo) {
      Box(modifier = Modifier.fillMaxSize()) {
        pt.avante.app.ui.layout.AdaptiveContent(maxWidth = 960.dp) {
            Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(title = "Pesquisa")

            TabRow(
                selectedTabIndex = searchScope.ordinal,
                containerColor = Cores.fundo,
                contentColor = Cores.acento,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[searchScope.ordinal]),
                        color = Cores.acento
                    )
                }
            ) {
                SearchScope.entries.forEach { scope ->
                    Tab(
                        selected = searchScope == scope,
                        onClick = { searchScope = scope },
                        text = {
                            Text(scope.label, color = if (searchScope == scope) Cores.acento else Cores.textoSecundario)
                        }
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().padding(Spacing.m),
                placeholder = { Text(searchScope.placeholder, color = Cores.textoSecundario) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                singleLine = true,
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cores.acento,
                    unfocusedBorderColor = Cores.contorno,
                    cursorColor = Cores.acento,
                    focusedTextColor = Cores.textoPrimario,
                    unfocusedTextColor = Cores.textoPrimario,
                    focusedPlaceholderColor = Cores.textoSecundario,
                    unfocusedPlaceholderColor = Cores.textoSecundario,
                    focusedLeadingIconColor = Cores.acento,
                    unfocusedLeadingIconColor = Cores.textoSecundario
                )
            )

            when (searchScope) {
                SearchScope.CURRENT_EDITION -> {
                    val currentEditionFeed = (homeUiState as? HomeUiState.Success)?.feed
                        ?.takeIf { it.editionNumber == viewModel.lastKnownCurrentIssueNumber }
                    LaunchedEffect(currentEditionFeed) {
                        viewModel.onCurrentEditionLoaded(currentEditionFeed?.articles.orEmpty())
                    }

                    if (currentEditionFeed == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (homeUiState is HomeUiState.Loading) "A carregar edição..." else "Pesquisa na edição atual indisponível.\nLigue-se à internet para obter a edição atual.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Cores.textoPrimario,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(Spacing.xl)
                            )
                        }
                    } else {
                        if (editionResults.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (searchQuery.isBlank()) "Pesquise artigos na edição atual." else "Nenhum resultado encontrado na edição atual.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Cores.textoPrimario,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(Spacing.xl)
                                )
                            }
                        } else {
                            val bookmarkedArticles by viewModel.bookmarkedArticles.collectAsState()
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = Spacing.l, start = Spacing.m, end = Spacing.m)
                            ) {
                                items(editionResults) { article ->
                                    Box(modifier = Modifier.padding(bottom = Spacing.m)) {
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
                }
                SearchScope.CADERNO -> {
                    if (noteResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isBlank()) "Pesquise no seu caderno." else "Nenhum resultado encontrado no caderno.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Cores.textoPrimario,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(Spacing.xl)
                            )
                        }
                    } else {
                        NoteList(notes = noteResults, onOpen = onNoteClick, onDelete = deleteNote)
                    }
                }
            }
        }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
      }
    }
}
