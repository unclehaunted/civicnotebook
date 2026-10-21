package pt.avante.app.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import pt.avante.app.BuildConfig
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
    var showAbout by rememberSaveable { mutableStateOf(false) }

    val homeUiState by homeViewModel.uiState.collectAsState()
    
    // Attempt to reconstruct ArticleSummary if needed from HomeFeed based on viewingArticleUrl
    val viewingArticle = remember(viewingArticleUrl, homeUiState) {
        if (viewingArticleUrl == null) null
        else (homeUiState as? HomeUiState.Success)?.feed?.articles?.find { it.url == viewingArticleUrl }
    }

    val isReading = viewingArticleUrl != null || viewingArticleId != null
    val isFullScreen = isReading || editingNoteId != null || showSettings || showAbout

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
                MainBottomBar(currentTab = currentTab, onTabSelected = { currentTab = it })
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
                    MainDestination.entries.forEach { dest ->
                        NavigationRailItem(
                            icon = { Icon(dest.icon, contentDescription = null) },
                            label = { Text(dest.label) },
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
            val backEnabled = showMenuSheet || isFullScreen
            androidx.activity.compose.BackHandler(enabled = backEnabled) {
                if (showMenuSheet) {
                    showMenuSheet = false
                } else if (showAbout) {
                    showAbout = false
                } else if (showSettings) {
                    showSettings = false
                } else if (editingNoteId != null) {
                    closeEditor()
                } else if (isReading) {
                    exitReader()
                }
            }

            if (showAbout) {
                SobreScreen(
                    versionName = BuildConfig.VERSION_NAME,
                    onBackClick = { showAbout = false }
                )
            } else if (showSettings) {
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
            } else if (isReading) {
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
            MainMenu(
                onSubscription = {
                    showMenuSheet = false
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AvanteSite.SUBSCRIPTION_URL))
                    context.startActivity(intent)
                },
                onSettings = {
                    showMenuSheet = false
                    showSettings = true
                },
                onAbout = {
                    showMenuSheet = false
                    showAbout = true
                }
            )
        }
    }
}
