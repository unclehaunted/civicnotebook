package pt.avante.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.*
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.ArticleBlock
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.TextPrimary
import pt.avante.app.theme.TextSecondary
import pt.avante.app.ui.components.CategoryRubricPill
import pt.avante.app.ui.settings.LocalReaderSettings
import pt.avante.app.ui.presentation.ArticleAuthorFormatter
import pt.avante.app.ui.presentation.ArticleTaxonomyFormatter
import pt.avante.app.viewmodel.ReaderMode
import pt.avante.app.viewmodel.ReaderUiState
import pt.avante.app.viewmodel.ReaderViewModel
import pt.avante.app.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    articleSummary: ArticleSummary? = null,
    articleId: String? = null,
    currentIssueNumber: String?,
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit,
    onOpenNote: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedHighlight by viewModel.selectedHighlight.collectAsState()
    val readerTheme = LocalReaderSettings.current.theme
    val readerBackground = Color(readerTheme.backgroundColor)
    val readerTextPrimary = Color(readerTheme.textColor)
    val readerTextSecondary = Color(readerTheme.secondaryTextColor)

    LaunchedEffect(articleSummary, articleId) {
        if (articleSummary != null) {
            viewModel.loadArticle(articleSummary, currentIssueNumber ?: "")
        } else if (articleId != null) {
            viewModel.loadArticleById(articleId)
        }
    }

    when (val state = uiState) {
        is ReaderUiState.Loading -> {
            Surface(modifier = Modifier.fillMaxSize(), color = readerBackground) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OriginalRed)
                }
            }
        }
        is ReaderUiState.LoadingWithPreview -> {
            Surface(modifier = Modifier.fillMaxSize(), color = readerBackground) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ReaderHero(
                            title = state.summary.title,
                            categoryLabel = state.summary.categoryLabel,
                            subcategoryLabel = state.summary.subcategoryLabel,
                            publication = pt.avante.app.data.Publication.AVANTE,
                            issueNumber = null,
                            author = state.summary.author,
                            displayImage = state.summary.heroImageUrl,
                            currentIssueNumber = currentIssueNumber
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.m)
                            ) {
                                CircularProgressIndicator(color = OriginalRed, modifier = Modifier.size(32.dp))
                                Text(
                                    text = "A carregar artigo...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = readerTextSecondary
                                )
                            }
                        }
                    }
                    
                    ReaderFloatingActions(
                        article = null,
                        articleUrl = "",
                        readerMode = ReaderMode.READ_MODE,
                        isRolledUp = false,
                        showShareFab = false,
                        onBackClick = onBackClick,
                        onToggleAnnotationMode = { },
                        onCreateNoteClick = { },
                        onShareClick = { }
                    )
                }
            }
        }
        is ReaderUiState.Error -> {
            ReaderErrorContent(
                reason = state.reason,
                onRetry = {
                    if (articleSummary != null) viewModel.loadArticle(articleSummary, currentIssueNumber ?: "")
                    else if (articleId != null) viewModel.loadArticleById(articleId)
                },
                onBackClick = onBackClick
            )
        }
        is ReaderUiState.SubscriptionRequired -> {
            // Similar to error but specifically for paywall
        }
        is ReaderUiState.Success -> {
            val article = state.article
            val displayImage = article.heroImageUrl
            
            val stableId = pt.avante.app.data.ArticleIdentity.generateStableId(article.canonicalUrl ?: articleSummary?.url ?: "")
            val savedPos = viewModel.scrollPositions[stableId] ?: (0 to 0)
            
            val listState = androidx.compose.runtime.key(stableId) {
                rememberLazyListState(initialFirstVisibleItemIndex = savedPos.first, initialFirstVisibleItemScrollOffset = savedPos.second)
            }
            
            LaunchedEffect(stableId, listState) {
                snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }.collect {
                    viewModel.scrollPositions[stableId] = it
                }
            }
            var headerHeight by remember { mutableStateOf(280.dp) }
            
            val readerMode by viewModel.readerMode.collectAsState()
            val articleHighlights by viewModel.articleHighlights.collectAsState()
            val hasArticleNotes by viewModel.hasArticleNotes.collectAsState()
            val selectedPassageIndex by viewModel.selectedPassageIndex.collectAsState()


            val scrollOffset = if (listState.firstVisibleItemIndex > 0) 600f else listState.firstVisibleItemScrollOffset.toFloat()
            val isRolledUp = scrollOffset > 150f
            
            val showShareFab = (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 200) && readerMode == ReaderMode.READ_MODE

            val viewport = pt.avante.app.ui.layout.LocalAppViewport.current
            val context = androidx.compose.ui.platform.LocalContext.current
            
            val onShareClick = {
                val articleUrl = articleSummary?.url ?: article.canonicalUrl ?: ""
                val shareText = buildString {
                    appendLine(article.title)
                    if (article.summary.isNotBlank()) {
                        appendLine()
                        appendLine(article.summary)
                    }
                    appendLine()
                    appendLine("Lê o artigo completo no Avante!:")
                    append(articleUrl)
                }
                val sendIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
            
            if (viewport.useWideReader) {
                ReaderWideLayout(
                    article = article,
                    articleSummary = articleSummary,
                    currentIssueNumber = currentIssueNumber,
                    displayImage = displayImage,
                    listState = listState,
                    scrollOffset = scrollOffset,
                    readerMode = readerMode,
                    selectedPassageIndex = selectedPassageIndex,
                    articleHighlights = articleHighlights,
                    hasArticleNotes = hasArticleNotes,
                    onBackClick = onBackClick,
                    onToggleAnnotationMode = { viewModel.toggleAnnotationMode() },
                    onCreateNoteClick = {
                        val noteId = viewModel.createArticleNote("Nota: ${article.title}")
                        if (noteId != null) onOpenNote(noteId)
                    },
                    onHighlightClick = { highlight -> viewModel.selectHighlight(highlight) },
                    onHighlightPassage = { text, index, start, end -> viewModel.highlightPassage(text, index, start, end) },
                    onCreateNote = { text, index, start, end ->
                        val noteId = viewModel.createArticleNote(
                            title = "Nota de Excerto",
                            initialBody = "## Excerto\n\n> $text\n\n## Reflexão\n\n"
                        )
                        if (noteId != null) {
                            viewModel.highlightPassage(text, index, start, end)
                            onOpenNote(noteId)
                        }
                    },
                    onShareClick = onShareClick
                )
            } else {
                ReaderCompactLayout(
                    article = article,
                    articleSummary = articleSummary,
                    currentIssueNumber = currentIssueNumber,
                    displayImage = displayImage,
                    listState = listState,
                    scrollOffset = scrollOffset,
                    isRolledUp = isRolledUp,
                    showShareFab = showShareFab,
                    readerMode = readerMode,
                    selectedPassageIndex = selectedPassageIndex,
                    articleHighlights = articleHighlights,
                    hasArticleNotes = hasArticleNotes,
                    onBackClick = onBackClick,
                    onToggleAnnotationMode = { viewModel.toggleAnnotationMode() },
                    onCreateNoteClick = {
                        val noteId = viewModel.createArticleNote("Nota: ${article.title}")
                        if (noteId != null) onOpenNote(noteId)
                    },
                    onHighlightClick = { highlight -> viewModel.selectHighlight(highlight) },
                    onHighlightPassage = { text, index, start, end -> viewModel.highlightPassage(text, index, start, end) },
                    onCreateNote = { text, index, start, end ->
                        val noteId = viewModel.createArticleNote(
                            title = "Nota de Excerto",
                            initialBody = "## Excerto\n\n> $text\n\n## Reflexão\n\n"
                        )
                        if (noteId != null) {
                            viewModel.highlightPassage(text, index, start, end)
                            onOpenNote(noteId)
                        }
                    },
                    onShareClick = onShareClick
                )
            }
            
            val selectedPassageText = if (selectedPassageIndex != null) {
                article.blocks[selectedPassageIndex!!].selectableText.orEmpty()
            } else null

            ReaderAnnotationSheets(
                selectedPassageText = selectedPassageText,
                onDismissSelection = { viewModel.clearSelection() },
                onHighlightSelection = {
                    viewModel.highlightPassage(selectedPassageText ?: "", selectedPassageIndex!!)
                    viewModel.clearSelection()
                },
                onCreateNoteFromSelection = {
                    val noteId = viewModel.createArticleNote("Excerto — ${article.title.take(30)}...", "## Excerto\n\n> $selectedPassageText\n\n## Reflexão\n\n")
                    viewModel.clearSelection()
                    if (noteId != null) {
                        onOpenNote(noteId)
                    }
                },
                
                selectedHighlight = selectedHighlight,
                onDismissHighlight = { viewModel.selectHighlight(null) },
                onCreateNoteFromHighlight = {
                    val initialBody = "## Excerto\n\n> ${selectedHighlight!!.selectedText}\n\n## Reflexão\n\n"
                    val noteId = viewModel.createArticleNote("Nota de: ${article.title.take(30)}...", initialBody)
                    if (noteId != null) {
                        viewModel.selectHighlight(null)
                        onOpenNote(noteId)
                    }
                },
                onDeleteHighlight = {
                    viewModel.removeHighlight(selectedHighlight!!.highlightId)
                    viewModel.selectHighlight(null)
                }
            )
        }
    }
}
