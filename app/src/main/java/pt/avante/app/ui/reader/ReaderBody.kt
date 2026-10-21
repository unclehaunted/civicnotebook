package pt.avante.app.ui.reader

import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.avante.app.data.Article
import pt.avante.app.data.ArticleBlock
import pt.avante.app.data.ArticleSpacing
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.theme.Cores
import pt.avante.app.theme.Spacing
import pt.avante.app.ui.settings.LocalReaderSettings

/** Altura livre no fim do artigo para o botão flutuante não tapar o último parágrafo: um tamanho, não espaçamento. */
private val FabClearance = 96.dp

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
        val readerSettings = LocalReaderSettings.current
        // Largura do texto em ems do corpo (#12): cresce com o Tamanho e com o tamanho do sistema.
        val columnMaxWidth = with(LocalDensity.current) {
            ArticleSpacing.resolve(readerSettings.fontSize, readerSettings.spacing).columnMaxWidthSp.sp.toDp()
        }
        val backgroundColor = Cores.fundo

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentPadding = PaddingValues(bottom = FabClearance)
        ) {
        item {
            heroContent()
        }

        item(key = "paper-start") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-28).dp)
                    .clip(
                        MaterialTheme.shapes.extraLarge.copy(
                            bottomStart = ZeroCornerSize,
                            bottomEnd = ZeroCornerSize
                        )
                    )
                    .background(backgroundColor)
                    .padding(top = Spacing.xl),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(modifier = Modifier.articleColumn(columnMaxWidth)) {
                    ReaderProvenance(
                        publication = article.publication,
                        issueNumber = article.issueNumber,
                        author = article.author,
                        currentIssueNumber = currentIssueNumber,
                        subcategoryLabel = article.subcategoryLabel,
                        hasNotes = hasArticleNotes,
                        hasHighlights = articleHighlights.isNotEmpty(),
                        textColor = Cores.textoSecundario
                    )
                    Spacer(modifier = Modifier.height(Spacing.m))
                    androidx.compose.material3.Divider(
                        color = Cores.divisor,
                        thickness = 1.dp
                    )
                    
                    if (hasArticleNotes) {
                        Spacer(modifier = Modifier.height(Spacing.m))
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.xs)
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Anotado",
                                tint = Cores.textoSecundario,
                                modifier = Modifier.size(16.dp)
                            )
                            androidx.compose.material3.Text(
                                text = "Anotado",
                                style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                color = Cores.textoSecundario,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(Spacing.l))
                    } else {
                        Spacer(modifier = Modifier.height(Spacing.l))
                    }
                }
            }
        }

        // Determine final text block for the end mark
        val hasPaywall = article.blocks.any { it is ArticleBlock.PaywallCTA }
        val lastTextBlockIndex = endMarkBlockIndex(article.blocks)

        // Render Blocks lazily
        itemsIndexed(article.blocks) { index, block ->
            val isSelected = index == selectedPassageIndex
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Box(modifier = Modifier.articleColumn(columnMaxWidth)) {
                    ArticleBlockRenderer(
                        block = block,
                        next = article.blocks.getOrNull(index + 1),
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xl),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Box(modifier = Modifier.articleColumn(columnMaxWidth)) {
                        ReaderEndMark()
                    }
                }
            }
        }
    }
}
}

/** A coluna do artigo: o texto mede no máximo [maxTextWidth], com a margem lateral por fora. */
private fun Modifier.articleColumn(maxTextWidth: Dp): Modifier =
    widthIn(max = maxTextWidth + Spacing.l * 2).padding(horizontal = Spacing.l)

/**
 * Índice do bloco que leva a marca de fim do artigo: o último bloco de texto selecionável
 * (Text, Question ou Quote), ou -1 se o artigo termina no paywall ou não tem texto.
 */
internal fun endMarkBlockIndex(blocks: List<ArticleBlock>): Int =
    if (blocks.any { it is ArticleBlock.PaywallCTA }) -1
    else blocks.indexOfLast { it.selectableText != null }
