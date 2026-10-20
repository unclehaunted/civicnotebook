package pt.avante.app.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.avante.AvanteSite
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.TextSecondary
import pt.avante.app.ui.components.*
import pt.avante.app.ui.layout.AdaptiveAppScaffold
import pt.avante.app.ui.layout.LocalAppViewport
import pt.avante.app.ui.reader.ArticleReaderScreen
import pt.avante.app.ui.screens.*
import pt.avante.app.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    db: AppDatabase,
    homeViewModel: HomeViewModel,
    pesquisaViewModel: PesquisaViewModel,
    cadernoViewModel: CadernoViewModel,
    readerViewModel: ReaderViewModel,
    noteViewModel: NoteViewModel,
    readerSettingsStore: pt.avante.app.data.ReaderSettingsStore,
    authRepository: pt.avante.app.data.auth.AvanteAuthRepository
) {
    var currentTab by rememberSaveable { mutableStateOf(MainDestination.EDICAO) }
    var viewingArticleUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var viewingArticleId by rememberSaveable { mutableStateOf<String?>(null) }
    var viewingArticleEdition by rememberSaveable { mutableStateOf<String>("") }
    var editingNoteId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingNewNote by rememberSaveable { mutableStateOf(false) }
    var showMenuSheet by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val homeUiState by homeViewModel.uiState.collectAsState()
    
    // Attempt to reconstruct ArticleSummary if needed from HomeFeed based on viewingArticleUrl
    val viewingArticle = remember(viewingArticleUrl, homeUiState) {
        if (viewingArticleUrl == null) null
        else (homeUiState as? HomeUiState.Success)?.feed?.articles?.find { it.url == viewingArticleUrl }
    }

    val tabs = listOf(
        MainDestination.EDICAO to Icons.AutoMirrored.Filled.MenuBook,
        MainDestination.CADERNO to Icons.AutoMirrored.Filled.Note,
        MainDestination.ARQUIVO to Icons.Default.Folder,
        MainDestination.PESQUISA to Icons.Default.Search
    )

    val getTitle = { dest: MainDestination ->
        when (dest) {
            MainDestination.EDICAO -> "Edição"
            MainDestination.CADERNO -> "Caderno"
            MainDestination.ARQUIVO -> "Arquivo"
            MainDestination.PESQUISA -> "Pesquisa"
        }
    }

    val isFullScreen = viewingArticleUrl != null || viewingArticleId != null || editingNoteId != null || showSettings

    // Voltar no leitor: sai primeiro do Modo de Anotação, depois do artigo.
    fun exitReader() {
        val wasAnnotating = readerViewModel.readerMode.value == ReaderMode.ANNOTATION_MODE
        readerViewModel.resetReaderState()
        if (!wasAnnotating) {
            viewingArticleUrl = null
            viewingArticleId = null
            viewingArticleEdition = ""
        }
    }

    // Sair do editor por qualquer caminho: o NoteViewModel descarta uma Nova Nota que ficou vazia.
    fun closeEditor() {
        noteViewModel.closeNote()
        editingNoteId = null
        editingNewNote = false
    }

    AdaptiveAppScaffold(
        bottomBar = {
            if (!isFullScreen) {
                var isNavExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                Surface(
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier.animateContentSize()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (isNavExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isNavExpanded = false }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Recolher",
                                    tint = TextSecondary,
                                    modifier = Modifier.align(Alignment.Center).padding(8.dp)
                                )
                            }
                            NavigationBar(containerColor = Color.White) {
                                tabs.forEach { (dest, icon) ->
                                    NavigationBarItem(
                                        icon = { Icon(icon, contentDescription = getTitle(dest)) },
                                        label = { Text(getTitle(dest)) },
                                        selected = currentTab == dest,
                                        onClick = { 
                                            currentTab = dest
                                            isNavExpanded = false
                                        },
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
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isNavExpanded = true }
                                    .padding(vertical = 12.dp)
                            ) {
                                val currentIcon = tabs.find { it.first == currentTab }?.second ?: Icons.Default.Home
                                Icon(
                                    imageVector = Icons.Default.ExpandLess,
                                    contentDescription = "Expandir menu",
                                    tint = TextSecondary,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Icon(
                                    imageVector = currentIcon,
                                    contentDescription = null,
                                    tint = OriginalRed,
                                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        navigationRail = {
            if (!isFullScreen) {
                NavigationRail(
                    containerColor = Color.White,
                    modifier = Modifier.fillMaxHeight(),
                    header = {
                        // Optional header content like a logo
                    }
                ) {
                    tabs.forEach { (dest, icon) ->
                        NavigationRailItem(
                            icon = { Icon(icon, contentDescription = getTitle(dest)) },
                            label = { Text(getTitle(dest)) },
                            selected = currentTab == dest,
                            onClick = { currentTab = dest },
                            colors = NavigationRailItemDefaults.colors(
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
    ) { innerPadding ->
      CompositionLocalProvider(
        pt.avante.app.ui.settings.LocalOpenSettings provides ({ showSettings = true } as () -> Unit)
      ) {
        Box(modifier = Modifier.padding(innerPadding)) {
            // Back handler hierarchy
            val backEnabled = showMenuSheet || showSettings || editingNoteId != null || viewingArticleUrl != null || viewingArticleId != null
            androidx.activity.compose.BackHandler(enabled = backEnabled) {
                if (showMenuSheet) {
                    showMenuSheet = false
                } else if (showSettings) {
                    showSettings = false
                } else if (editingNoteId != null) {
                    closeEditor()
                } else if (viewingArticleUrl != null || viewingArticleId != null) {
                    exitReader()
                }
            }

            if (showSettings) {
                DefinicoesScreen(
                    settingsStore = readerSettingsStore,
                    authRepository = authRepository,
                    onBackClick = { showSettings = false }
                )
            } else if (editingNoteId != null) {
                NoteEditorScreen(
                    noteId = editingNoteId!!,
                    isNewNote = editingNewNote,
                    viewModel = noteViewModel,
                    onBackClick = { closeEditor() },
                    onOpenArticle = {
                        viewingArticleUrl = null
                        viewingArticleId = it
                        closeEditor()
                    }
                )
            } else if (viewingArticleUrl != null || viewingArticleId != null) {
                ArticleReaderScreen(
                    articleSummary = viewingArticle,
                    articleId = viewingArticleId,
                    currentIssueNumber = viewingArticleEdition,
                    viewModel = readerViewModel,
                    onBackClick = { exitReader() },
                    onOpenNote = { editingNoteId = it }
                )
            } else {
                when (currentTab) {
                    MainDestination.EDICAO -> pt.avante.app.ui.home.HomeFeedScreen(
                        viewModel = homeViewModel,
                        onArticleClick = { article: pt.avante.app.data.ArticleSummary, edition: String -> 
                            viewingArticleId = null
                            viewingArticleUrl = article.url 
                            viewingArticleEdition = edition
                        },
                        onFeedLoaded = { },
                        onSearchClick = { currentTab = MainDestination.PESQUISA },
                        onMenuClick = { showMenuSheet = true }
                    )
                    MainDestination.CADERNO -> CadernoScreen(
                        viewModel = cadernoViewModel,
                        onNoteClick = { editingNoteId = it },
                        onNewNote = {
                            editingNewNote = true
                            editingNoteId = it
                        }
                    )
                    MainDestination.ARQUIVO -> ArquivoScreen(isSubscriber = false, isOnline = false)
                    MainDestination.PESQUISA -> {
                        PesquisaScreen(
                            viewModel = pesquisaViewModel,
                            homeUiState = homeUiState,
                            onArticleClick = { article ->
                                viewingArticleId = null
                                viewingArticleUrl = article.url
                                viewingArticleEdition = (homeUiState as? HomeUiState.Success)?.feed?.editionNumber ?: ""
                            },
                            onNoteClick = { editingNoteId = it }
                        )
                    }
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
            val context = LocalContext.current
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Menu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                
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
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AvanteSite.SUBSCRIPTION_URL))
                        context.startActivity(intent)
                    }
                )
                ListItem(
                    headlineContent = { Text("Definições") },
                    leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.clickable {
                        showMenuSheet = false
                        showSettings = true
                    }
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
