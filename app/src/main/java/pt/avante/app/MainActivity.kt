package pt.avante.app

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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
import pt.avante.app.ui.reader.*
import pt.avante.app.ui.components.*
import pt.avante.app.ui.layout.ProvideAppViewport
import pt.avante.app.ui.main.MainApp

import androidx.activity.viewModels
import pt.avante.app.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = AppDatabase.getInstance(applicationContext)
        val currentIssueStore = pt.avante.app.data.CurrentIssueStore(applicationContext)
        val factory = AppViewModelFactory(db, currentIssueStore)

        val readerSettingsStore = pt.avante.app.data.ReaderSettingsStore(applicationContext)
        val sessionStore = pt.avante.app.data.auth.SessionStore(applicationContext)
        val authRepository = pt.avante.app.data.auth.AvanteAuthRepository(sessionStore)
        pt.avante.app.data.auth.AvanteHttp.init(authRepository)
        
        lifecycleScope.launch {
            db.articleDao().sanitizeStaleHeroes()
        }
        
        val homeViewModel: HomeViewModel by viewModels { factory }
        val pesquisaViewModel: PesquisaViewModel by viewModels { factory }
        val cadernoViewModel: CadernoViewModel by viewModels { factory }
        val readerViewModel: ReaderViewModel by viewModels { factory }
        val noteViewModel: NoteViewModel by viewModels { factory }
        
        setContent {
            OriginalTheme {
                val readerSettings by readerSettingsStore.settings.collectAsState()
                CompositionLocalProvider(
                    pt.avante.app.ui.settings.LocalReaderSettings provides readerSettings
                ) {
                    ProvideAppViewport {
                        MainApp(db, homeViewModel, pesquisaViewModel, cadernoViewModel, readerViewModel, noteViewModel, readerSettingsStore, authRepository)
                    }
                }
            }
        }
    }
}
