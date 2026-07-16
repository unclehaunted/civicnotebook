package pt.avante.app.ui.screens


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import pt.avante.app.data.ArticleSummary
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.view.View
import pt.avante.app.data.HomeFeed
import pt.avante.app.theme.*
import kotlinx.coroutines.launch

import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import pt.avante.app.viewmodel.ReaderViewModel
import pt.avante.app.viewmodel.ReaderUiState
import androidx.compose.animation.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    articleSummary: ArticleSummary? = null,
    articleId: String? = null,
    editionNumber: String,
    viewModel: ReaderViewModel,
    onBackClick: () -> Unit,
    onOpenNote: (String) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedHighlight by viewModel.selectedHighlight.collectAsState()

    LaunchedEffect(articleSummary, articleId) {
        if (articleSummary != null) {
            viewModel.loadArticle(articleSummary, editionNumber)
        } else if (articleId != null) {
            viewModel.loadArticleById(articleId)
        }
    }

    when (val state = uiState) {
        is ReaderUiState.Loading -> {
            Surface(modifier = Modifier.fillMaxSize(), color = WarmPaper) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OriginalRed)
                }
            }
        }
        is ReaderUiState.Error -> {
            Surface(modifier = Modifier.fillMaxSize(), color = WarmPaper) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, contentDescription = "Erro", tint = OriginalRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(state.message, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                if (articleSummary != null) viewModel.loadArticle(articleSummary, editionNumber) 
                                else if (articleId != null) viewModel.loadArticleById(articleId)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OriginalRed)
                        ) {
                            Text("Tentar Novamente", color = Color.White)
                        }
                    }
                }
            }
        }
        is ReaderUiState.SubscriptionRequired -> {
            // Similar to error but specifically for paywall
        }
        is ReaderUiState.Success -> {
            val article = state.article
            val displayImage = article.heroImageUrl ?: articleSummary?.heroImageUrl ?: "https://avante.pt/get_img?ImageWidth=355&ImageHeight=237&ImageId=49919"
            ArticleParallaxView(
                article = article,
                displayImage = displayImage,
                editionNumber = editionNumber,
                articleUrl = articleSummary?.url ?: article.canonicalUrl ?: "",
                viewModel = viewModel,
                onBack = onBackClick,
                onOpenNote = onOpenNote
            )

            if (selectedHighlight != null) {
                HighlightDetailsSheet(
                    highlight = selectedHighlight!!,
                    onDismiss = { viewModel.selectHighlight(null) },
                    onCreateNote = {
                        val initialBody = "## Excerto\n\n> ${selectedHighlight!!.selectedText}\n\n## Reflexão\n\n"
                        val noteId = viewModel.createArticleNote("Nota de: ${article.title.take(30)}...", initialBody)
                        if (noteId != null) {
                            viewModel.selectHighlight(null)
                            onOpenNote(noteId)
                        }
                    },
                    onDelete = {
                        viewModel.removeHighlight(selectedHighlight!!.highlightId)
                        viewModel.selectHighlight(null)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleParallaxView(article: Article, displayImage: String, editionNumber: String, articleUrl: String, viewModel: ReaderViewModel, onBack: () -> Unit, onOpenNote: (String) -> Unit) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val density = LocalDensity.current
    var headerHeight by remember { mutableStateOf(280.dp) }
    
    val readerMode by viewModel.readerMode.collectAsState()
    val selectedPassageIndex by viewModel.selectedPassageIndex.collectAsState()
    val articleHighlights by viewModel.articleHighlights.collectAsState()

    var showCommentDialog by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    
    // Calculate scroll progress
    val scrollOffset = if (listState.firstVisibleItemIndex > 0) 600f else listState.firstVisibleItemScrollOffset.toFloat()
    val isRolledUp = scrollOffset > 150f
    val buttonBorderModifier = if (isRolledUp) Modifier.border(1.dp, OriginalRed.copy(alpha = 0.5f), CircleShape) else Modifier
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Layer: Photo and Fixed Title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp) // Minimum height, expands if title is very long
                .onGloballyPositioned { coordinates ->
                    val heightDp = with(density) { coordinates.size.height.toDp() }
                    if (heightDp > 0.dp) headerHeight = heightDp
                }
                .graphicsLayer {
                    translationY = -scrollOffset * 0.8f // Faster parallax to move it out of the way
                    val heightPx = headerHeight.toPx()
                    if (heightPx > 0) {
                        alpha = (1f - (scrollOffset / heightPx) * 1.2f).coerceIn(0f, 1f)
                    }
                }
        ) {
            // Big photo covering the head space
            AsyncImage(
                model = displayImage,
                contentDescription = "Imagem do Artigo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            
            // Gradient overlay to ensure white text is readable
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 32.dp)
            ) {
                Surface(
                    color = OriginalRed,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = article.section?.uppercase() ?: article.publication.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )
            }
        }

        // Foreground Scrolling Layer
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp) // Room for FAB
        ) {
            // Spacer that exactly matches the dynamic header height
            item {
                Spacer(modifier = Modifier.height(headerHeight))
            }

            // Annotation Mode Banner
            if (readerMode == pt.avante.app.viewmodel.ReaderMode.ANNOTATION_MODE) {
                item {
                    Surface(
                        color = OriginalRed,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Modo de Anotação Ativo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                "Selecione qualquer texto do artigo para realçar, comentar ou criar uma nota.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Body content card sliding UP over the photo layer
            item {
                val cornerRadius = (32f - (scrollOffset * 0.05f)).coerceIn(0f, 32f).dp
                Surface(
                    color = WarmPaper,
                    shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
                    modifier = Modifier.fillMaxWidth() // Perfectly aligned under the title
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp, bottom = 32.dp)
                    ) {
                        Text(
                            text = articleMetaLine(article, editionNumber),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        HorizontalDivider(color = SoftDivider, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Render Blocks
                        article.blocks.forEachIndexed { index, block ->
                            val isSelected = index == selectedPassageIndex
                            val blockModifier = Modifier

                            when (block) {
                                is ArticleBlock.Text -> {
                                    AndroidView(
                                        modifier = Modifier.padding(bottom = 16.dp),
                                        factory = { ctx ->
                                            android.widget.TextView(ctx).apply {
                                                setTextIsSelectable(true)
                                                movementMethod = LinkMovementMethod.getInstance()
                                                highlightColor = android.graphics.Color.TRANSPARENT
                                                
                                                val typeface = androidx.core.content.res.ResourcesCompat.getFont(ctx, pt.avante.app.R.font.libre_franklin)
                                                setTypeface(typeface)
                                                textSize = 18f
                                                setTextColor(android.graphics.Color.parseColor("#333333"))
                                                setLineSpacing(0f, 1.4f)
                                                
                                                customSelectionActionModeCallback = object : android.view.ActionMode.Callback {
                                                    override fun onCreateActionMode(mode: android.view.ActionMode, menu: android.view.Menu): Boolean {
                                                        menu.add(0, 1, 0, "Realçar")
                                                        menu.add(0, 2, 1, "Criar Nota")
                                                        return true
                                                    }
                                                    override fun onPrepareActionMode(mode: android.view.ActionMode, menu: android.view.Menu): Boolean = false
                                                    override fun onActionItemClicked(mode: android.view.ActionMode, item: android.view.MenuItem): Boolean {
                                                        val start = selectionStart
                                                        val end = selectionEnd
                                                        if (start < 0 || end < 0 || start >= end) return false
                                                        val selectedStr = text.substring(start, end)
                                                        when (item.itemId) {
                                                            1 -> {
                                                                viewModel.highlightPassage(selectedStr, index, start, end)
                                                                mode.finish()
                                                                return true
                                                            }
                                                            2 -> {
                                                                val noteId = viewModel.createArticleNote(title = "Nota de Excerto", initialBody = "## Excerto\n\n> $selectedStr\n\n## Reflexão\n\n")
                                                                if (noteId != null) {
                                                                    viewModel.highlightPassage(selectedStr, index, start, end) // also highlight it
                                                                    onOpenNote(noteId)
                                                                }
                                                                mode.finish()
                                                                return true
                                                            }
                                                        }
                                                        return false
                                                    }
                                                    override fun onDestroyActionMode(mode: android.view.ActionMode) {}
                                                }
                                            }
                                        },
                                        update = { view ->
                                            view.text = buildHighlightedText(
                                                text = block.content,
                                                highlights = articleHighlights,
                                                blockIndex = index,
                                                onHighlightClick = { highlight ->
                                                    viewModel.selectHighlight(highlight)
                                                }
                                            )
                                        }
                                    )
                                }
                                is ArticleBlock.Subheading -> {
                                    Text(
                                        text = block.text,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = TextPrimary,
                                        modifier = blockModifier.padding(top = 16.dp, bottom = 12.dp)
                                    )
                                }
                                is ArticleBlock.PaywallCTA -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://avante.pt/assinaturas"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = OriginalRed),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth(0.8f)
                                                .height(50.dp)
                                        ) {
                                            Text("Assina o Avante!", color = Color.White, style = MaterialTheme.typography.labelLarge)
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Já é assinante? Inicie sessão",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = OriginalRed,
                                            modifier = Modifier.clickable { /* TODO: Login Flow */ }
                                        )
                                    }
                                }
                                is ArticleBlock.Image -> { }
                            }
                        }
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 48.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
        }

        // Floating Top Right Buttons (Annotation / Note)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (readerMode == pt.avante.app.viewmodel.ReaderMode.READ_MODE) {
                IconButton(
                    onClick = { 
                        val noteId = viewModel.createArticleNote("Nota: ${article.title}") 
                        if (noteId != null) onOpenNote(noteId)
                    },
                    modifier = Modifier.size(48.dp).then(buttonBorderModifier).background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "Criar nota do artigo", tint = OriginalRed)
                }
                IconButton(
                    onClick = { viewModel.toggleAnnotationMode() },
                    modifier = Modifier.size(48.dp).then(buttonBorderModifier).background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Ativar Modo de Anotação", tint = OriginalRed)
                }
            } else {
                // Annotation mode active - show exit button with stronger visual
                Surface(
                    color = OriginalRed,
                    shape = CircleShape,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(48.dp).clickable { viewModel.toggleAnnotationMode() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Sair do Modo de Anotação",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        // Share FAB & Bottom Gradient Fade - Only visible when rolled up
        AnimatedVisibility(
            visible = (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 200) && readerMode == pt.avante.app.viewmodel.ReaderMode.READ_MODE,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, WarmPaper)
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    color = Color(0xFF1E1E1E), // Dark background for the pill
                    shape = RoundedCornerShape(32.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // White Share Button
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.clickable {
                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${article.title}\n\nLê o artigo completo no Avante!: $articleUrl")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Partilhar", tint = Color.Black, modifier = Modifier.size(20.dp))
                                Text("Partilhar", color = Color.Black, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }

    // Context Menu Bottom Sheet
    if (selectedPassageIndex != null) {
        val selectedBlock = article.blocks[selectedPassageIndex!!]
        val selectedText = when (selectedBlock) {
            is ArticleBlock.Text -> selectedBlock.content
            is ArticleBlock.Subheading -> selectedBlock.text
            else -> ""
        }

        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelection() },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(
                    text = "Ação de Anotação",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                                AnnotationActionRow(
                    icon = Icons.Default.BorderColor,
                    title = "Realçar excerto",
                    subtitle = "Guardar este trecho no artigo",
                    tint = AnnotationYellow,
                    background = AnnotationYellow.copy(alpha = 0.10f),
                    onClick = {
                        viewModel.highlightPassage(selectedText, selectedPassageIndex!!)
                        viewModel.clearSelection()
                    }
                )
                
                AnnotationActionRow(
                    icon = Icons.Default.ChatBubbleOutline,
                    title = "Comentar excerto",
                    subtitle = "Adicionar uma nota rápida ao realce",
                    tint = OriginalRed,
                    background = OriginalRed.copy(alpha = 0.06f),
                    onClick = {
                        showCommentDialog = true
                    }
                )

                AnnotationActionRow(
                    icon = Icons.Default.NoteAdd,
                    title = "Criar nota",
                    subtitle = "Abrir uma nova nota com este excerto",
                    tint = MutedGreen,
                    background = MutedGreen.copy(alpha = 0.07f),
                    onClick = {
                        val initialBody = "## Excerto\n\n> $selectedText\n\n## Reflexão\n\n"
                        val noteId = viewModel.createArticleNote("Excerto — ${article.title.take(30)}...", initialBody)
                        viewModel.clearSelection()
                        if (noteId != null) {
                            onOpenNote(noteId)
                        }
                    }
                )

                ListItem(
                    headlineContent = { Text("Adicionar a nota existente") },
                    leadingContent = { Icon(Icons.Default.LibraryAdd, contentDescription = null) },
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Brevemente...", Toast.LENGTH_SHORT).show()
                        viewModel.clearSelection()
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showCommentDialog && selectedPassageIndex != null) {
        val selectedBlock = article.blocks[selectedPassageIndex!!]
        val selectedText = when (selectedBlock) {
            is ArticleBlock.Text -> selectedBlock.content
            is ArticleBlock.Subheading -> selectedBlock.text
            else -> ""
        }

        AlertDialog(
            onDismissRequest = { showCommentDialog = false },
            title = { Text("Comentar excerto") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = WarmPaper,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "\"$selectedText\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("O seu comentário") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.commentOnPassage(selectedText, selectedPassageIndex!!, commentBody = commentText)
                    showCommentDialog = false
                    commentText = ""
                }) {
                    Text("Guardar", color = OriginalRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommentDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

fun buildHighlightedText(
    text: String,
    highlights: List<pt.avante.app.data.db.HighlightEntity>,
    blockIndex: Int,
    onHighlightClick: (pt.avante.app.data.db.HighlightEntity) -> Unit
): SpannableString {
    val spannable = SpannableString(text)

    highlights
        .filter { it.blockIndex == blockIndex && it.startOffset != null && it.endOffset != null }
        .forEach { highlight ->
            val start = highlight.startOffset!!.coerceIn(0, text.length)
            val end = highlight.endOffset!!.coerceIn(start, text.length)

            if (start < end) {
                spannable.setSpan(
                    BackgroundColorSpan(android.graphics.Color.argb(90, 214, 178, 94)),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                spannable.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            onHighlightClick(highlight)
                        }
                    },
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

    return spannable
}

fun articleMetaLine(article: Article, editionNumber: String): String {
    return listOfNotNull(
        "Avante",
        article.author.takeIf { it.isNotBlank() },
        editionNumber.takeIf { it.isNotBlank() }?.let { "Edição $it" },
        article.publishedAt.takeIf { it.isNotBlank() && it != editionNumber && it != "Hoje" }
    ).joinToString(" · ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightDetailsSheet(
    highlight: pt.avante.app.data.db.HighlightEntity,
    onDismiss: () -> Unit,
    onCreateNote: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                "O que quer fazer com este excerto?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = pt.avante.app.theme.TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Surface(
                color = pt.avante.app.theme.WarmPaper,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp)) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(48.dp)
                            .background(pt.avante.app.theme.OriginalRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\"${highlight.selectedText}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = pt.avante.app.theme.TextSecondary,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            ListItem(
                headlineContent = { Text("Criar nota") },
                leadingContent = { Icon(Icons.Default.NoteAdd, contentDescription = null, tint = pt.avante.app.theme.OriginalRed) },
                modifier = Modifier.clickable { onCreateNote() }
            )
            ListItem(
                headlineContent = { Text("Adicionar a uma nota (em breve)") },
                leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                modifier = Modifier.clickable { /* TODO */ }
            )
            ListItem(
                headlineContent = { Text("Remover realce") },
                leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = pt.avante.app.theme.OriginalRed) },
                modifier = Modifier.clickable { onDelete() }
            )
        }
    }
}

@Composable
fun AnnotationActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color,
    background: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        color = background,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(end = 16.dp))
            Column {
                Text(title, color = tint, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
