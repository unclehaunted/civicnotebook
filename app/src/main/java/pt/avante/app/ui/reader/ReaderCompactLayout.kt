package pt.avante.app.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.ui.layout.LocalAppViewport
import pt.avante.app.viewmodel.ReaderMode

@Composable
fun ReaderCompactLayout(
    article: Article,
    articleSummary: ArticleSummary?,
    currentIssueNumber: String?,
    displayImage: String?,
    listState: LazyListState,
    scrollOffset: Float,
    isRolledUp: Boolean,
    showShareFab: Boolean,
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
    val viewport = LocalAppViewport.current
    
    // Max header height constraint when image exists
    val maxHeaderHeight = if (viewport.isLandscape && viewport.isCompactHeight) 180.dp else androidx.compose.ui.unit.max(360.dp, viewport.height * 0.55f)

    Box(modifier = modifier.fillMaxSize()) {
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
                    modifier = if (!displayImage.isNullOrBlank()) Modifier.height(maxHeaderHeight) else Modifier
                )
            }
        )

        androidx.compose.animation.AnimatedVisibility(
            visible = readerMode == ReaderMode.ANNOTATION_MODE,
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(),
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.TopCenter)
                .padding(top = 80.dp) // Pushed down to clear the floating actions
                .fillMaxWidth(0.9f)
        ) {
            androidx.compose.material3.Surface(
                color = androidx.compose.ui.graphics.Color(0xFFFDECEA),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, pt.avante.app.theme.OriginalRed),
                shadowElevation = 4.dp
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        text = "Modo de Anotação Activo",
                        style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                        color = pt.avante.app.theme.OriginalRed,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    androidx.compose.material3.Text(
                        text = "Seleccione um excerto para criar uma nota",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = pt.avante.app.theme.TextSecondary
                    )
                }
            }
        }

        ReaderFloatingActions(
            article = article,
            articleUrl = articleSummary?.url ?: article.canonicalUrl ?: "",
            readerMode = readerMode,
            isRolledUp = isRolledUp,
            showShareFab = showShareFab,
            onBackClick = onBackClick,
            onToggleAnnotationMode = onToggleAnnotationMode,
            onCreateNoteClick = onCreateNoteClick,
            onShareClick = onShareClick
        )
    }
}
