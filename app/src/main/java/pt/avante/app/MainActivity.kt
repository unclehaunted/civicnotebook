package pt.avante.app

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
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
import pt.avante.app.viewmodel.CadernoViewModel
import pt.avante.app.viewmodel.ReaderViewModel
import pt.avante.app.viewmodel.NoteViewModel

import pt.avante.app.ui.screens.*
import pt.avante.app.ui.components.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "civicnotebook-db"
        ).fallbackToDestructiveMigration().build()
        
        val homeViewModel = HomeViewModel(db)
        val pesquisaViewModel = PesquisaViewModel(db)
        val cadernoViewModel = CadernoViewModel(db)
        val readerViewModel = ReaderViewModel(db)
        val noteViewModel = NoteViewModel(db)
        
        setContent {
            OriginalTheme {
                MainApp(db, homeViewModel, pesquisaViewModel, cadernoViewModel, readerViewModel, noteViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(db: AppDatabase, homeViewModel: HomeViewModel, pesquisaViewModel: PesquisaViewModel, cadernoViewModel: CadernoViewModel, readerViewModel: ReaderViewModel, noteViewModel: NoteViewModel) {
    var currentTab by remember { mutableStateOf("Edição") }
    var viewingArticle by remember { mutableStateOf<ArticleSummary?>(null) }
    var viewingArticleId by remember { mutableStateOf<String?>(null) }
    var editingNoteId by remember { mutableStateOf<String?>(null) }
    var showMenuSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        bottomBar = {
            if (viewingArticle == null && viewingArticleId == null && editingNoteId == null) {
                Surface(
                    color = Color.White,
                    shadowElevation = 16.dp
                ) {
                    NavigationBar(containerColor = Color.White) {
                        val tabs = listOf(
                            "Edição" to Icons.Default.MenuBook,
                            "Caderno" to Icons.Default.Note,
                            "Arquivo" to Icons.Default.Folder,
                            "Pesquisa" to Icons.Default.Search
                        )
                        tabs.forEach { (title, icon) ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = title) },
                                label = { Text(title) },
                                selected = currentTab == title,
                                onClick = { currentTab = title },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = OriginalRed,
                                    selectedTextColor = OriginalRed,
                                    indicatorColor = OriginalRed.copy(alpha = 0.1f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (editingNoteId != null) {
                NoteEditorScreen(
                    noteId = editingNoteId!!,
                    viewModel = noteViewModel,
                    onBackClick = { editingNoteId = null },
                    onOpenArticle = { 
                        viewingArticleId = it 
                        editingNoteId = null 
                    }
                )
            } else if (viewingArticle != null || viewingArticleId != null) {
                ArticleReaderScreen(
                    articleSummary = viewingArticle,
                    articleId = viewingArticleId,
                    editionNumber = "", // Edition number is now handled gracefully inside
                    viewModel = readerViewModel,
                    onBackClick = { 
                        viewingArticle = null
                        viewingArticleId = null
                    },
                    onOpenNote = { editingNoteId = it }
                )
            } else {
                when (currentTab) {
                    "Edição" -> HomeFeedScreen(
                        viewModel = homeViewModel,
                        onArticleClick = { viewingArticle = it },
                        onFeedLoaded = { /* No longer needed */ },
                        onSearchClick = { currentTab = "Pesquisa" },
                        onMenuClick = { showMenuSheet = true }
                    )
                    "Caderno" -> CadernoScreen(
                        viewModel = cadernoViewModel,
                        onNoteClick = { editingNoteId = it.noteId }
                    )
                    "Arquivo" -> ArquivoScreen(isSubscriber = false, isOnline = false)
                    "Pesquisa" -> PesquisaScreen(
                        viewModel = pesquisaViewModel,
                        onNoteClick = { editingNoteId = it }
                    )
                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("A vista $currentTab ainda não foi construída.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (showMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Menu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                
                val context = LocalContext.current
                
                ListItem(
                    headlineContent = { Text("Exportar notas") },
                    leadingContent = { Icon(Icons.Default.Download, contentDescription = null) },
                    modifier = Modifier.clickable { showMenuSheet = false }
                )
                ListItem(
                    headlineContent = { Text("Assinatura") },
                    leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
                    modifier = Modifier.clickable { 
                        showMenuSheet = false 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://avante.pt/assinaturas"))
                        context.startActivity(intent)
                    }
                )
                ListItem(
                    headlineContent = { Text("Definições") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.clickable { showMenuSheet = false }
                )
                ListItem(
                    headlineContent = { Text("Sobre") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                    modifier = Modifier.clickable { showMenuSheet = false }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
