package pt.avante.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.viewmodel.ReaderMode

@Composable
fun ReaderWideLayout(
    article: Article,
    articleSummary: ArticleSummary?,
    currentIssueNumber: String?,
    displayImage: String?,
    listState: LazyListState,
    scrollOffset: Float,
    readerMode: ReaderMode,
    selectedPassageIndex: Int?,
    articleHighlights: List<HighlightEntity>,
    hasArticleNotes: Boolean,
    onBackClick: () -> Unit,
    onToggleAnnotationMode: () -> Unit,
    onCreateNoteClick: () -> Unit,
    onHighlightClick: (HighlightEntity) -> Unit,
    onHighlightPassage: (String, Int, Int, Int) -> Unit,
    onCreateNote: (String, Int, Int, Int) -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(pt.avante.app.ui.settings.LocalReaderSettings.current.theme.backgroundColor)
    Box(modifier = modifier.fillMaxSize().background(backgroundColor)) {
        Row(modifier = Modifier.fillMaxSize()) {
            ReaderActionRail(
                modifier = Modifier.width(72.dp).fillMaxHeight(),
                readerMode = readerMode,
                onBackClick = onBackClick,
                onToggleAnnotationMode = onToggleAnnotationMode,
                onCreateNoteClick = onCreateNoteClick,
                onShareClick = onShareClick
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 1080.dp)
                        .fillMaxSize()
                ) {
                    ReaderBody(
                        article = article,
                        currentIssueNumber = currentIssueNumber,
                        listState = listState,
                        scrollOffset = scrollOffset,
                        selectedPassageIndex = selectedPassageIndex,
                        articleHighlights = articleHighlights,
                        hasArticleNotes = hasArticleNotes,
                        onHighlightClick = onHighlightClick,
                        onHighlightPassage = onHighlightPassage,
                        onCreateNote = onCreateNote,
                        heroContent = {
                            ReaderHero(
                                title = article.title,
                                categoryLabel = article.categoryLabel,
                                subcategoryLabel = article.subcategoryLabel,
                                publication = article.publication,
                                issueNumber = article.issueNumber,
                                author = article.author,
                                displayImage = displayImage,
                                currentIssueNumber = currentIssueNumber,
                                modifier = if (!displayImage.isNullOrBlank()) Modifier.heightIn(max = 400.dp) else Modifier
                            )
                        }
                    )

                    AnnotationModeBanner(
                        visible = readerMode == ReaderMode.ANNOTATION_MODE,
                        topOffset = 16.dp,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}
