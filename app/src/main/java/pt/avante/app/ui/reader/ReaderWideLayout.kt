package pt.avante.app.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.TextPrimary
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
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color(pt.avante.app.ui.settings.LocalReaderSettings.current.theme.backgroundColor))) {
        val displayWidth = maxWidth
        
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

                    androidx.compose.animation.AnimatedVisibility(
                        visible = readerMode == ReaderMode.ANNOTATION_MODE,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
                    ) {
                        Surface(
                            color = Color(0xFFFDECEA),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, OriginalRed),
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Modo de Anotação Activo",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = OriginalRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Seleccione um excerto para criar uma nota",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = pt.avante.app.theme.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
