package pt.avante.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pt.avante.app.data.Article
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.ui.settings.LocalReaderSettings

@Composable
fun ReaderBody(
    article: Article,
    currentIssueNumber: String?,
    listState: LazyListState,
    scrollOffset: Float,
    selectedPassageIndex: Int?,
    articleHighlights: List<HighlightEntity>,
    hasArticleNotes: Boolean = false,
    onHighlightClick: (HighlightEntity) -> Unit,
    onHighlightPassage: (String, Int, Int, Int) -> Unit,
    onCreateNote: (String, Int, Int, Int) -> Unit,
    heroContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        val density = LocalDensity.current.density
        val readerTheme = LocalReaderSettings.current.theme
        val backgroundColor = androidx.compose.ui.graphics.Color(readerTheme.backgroundColor)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentPadding = PaddingValues(bottom = 100.dp) // Room for FAB
        ) {
        item {
            heroContent()
        }

        item(key = "paper-start") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(backgroundColor)
                    .padding(top = 28.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(modifier = Modifier.widthIn(max = 720.dp).padding(horizontal = 24.dp)) {
                    ReaderProvenance(
                        publication = article.publication,
                        issueNumber = article.issueNumber,
                        author = article.author,
                        currentIssueNumber = currentIssueNumber,
                        subcategoryLabel = article.subcategoryLabel,
                        hasNotes = hasArticleNotes,
                        hasHighlights = articleHighlights.isNotEmpty(),
                        textColor = androidx.compose.ui.graphics.Color(readerTheme.secondaryTextColor)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Divider(
                        color = androidx.compose.ui.graphics.Color(readerTheme.secondaryTextColor).copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                    
                    if (hasArticleNotes) {
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Anotado",
                                tint = androidx.compose.ui.graphics.Color(0xFFB97A00),
                                modifier = Modifier.size(16.dp)
                            )
                            androidx.compose.material3.Text(
                                text = "Anotado",
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                color = androidx.compose.ui.graphics.Color(0xFFB97A00),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Determine final text block for the end mark
        val hasPaywall = article.blocks.any { it is pt.avante.app.data.ArticleBlock.PaywallCTA }
        val lastTextBlockIndex = if (hasPaywall) -1 else article.blocks.indexOfLast { 
            it is pt.avante.app.data.ArticleBlock.Text || it is pt.avante.app.data.ArticleBlock.Quote 
        }

        // Render Blocks lazily
        itemsIndexed(article.blocks) { index, block ->
            val isSelected = index == selectedPassageIndex
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Box(modifier = Modifier.widthIn(max = 720.dp).padding(horizontal = 24.dp)) {
                    ArticleBlockRenderer(
                        block = block,
                        index = index,
                        isSelected = isSelected,
                        isFinalCompleteTextBlock = index == lastTextBlockIndex,
                        articleHighlights = articleHighlights,
                        onHighlightClick = onHighlightClick,
                        onHighlightPassage = onHighlightPassage,
                        onCreateNote = onCreateNote
                    )
                }
            }
        }

        if (!hasPaywall) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Box(modifier = Modifier.widthIn(max = 720.dp).padding(horizontal = 24.dp)) {
                        ReaderEndMark()
                    }
                }
            }
        }
    }
}
}
