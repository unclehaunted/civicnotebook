package pt.avante.app.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

import pt.avante.app.*
import pt.avante.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PesquisaScreen(viewModel: PesquisaViewModel, onNoteClick: (String) -> Unit) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    
    Surface(modifier = Modifier.fillMaxSize(), color = WarmPaper) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { Text("Pesquisa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmPaper,
                    titleContentColor = TextPrimary
                )
            )
            HorizontalDivider(color = SoftDivider, thickness = 1.dp)
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Pesquisar no caderno...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OriginalRed,
                    unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                    cursorColor = OriginalRed,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedPlaceholderColor = TextSecondary,
                    unfocusedPlaceholderColor = TextSecondary,
                    focusedLeadingIconColor = OriginalRed,
                    unfocusedLeadingIconColor = TextSecondary
                )
            )
            
            if (searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isBlank()) "Pesquise no seu caderno e arquivo." else "Nenhum resultado encontrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(searchResults) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.noteId) },
                            onDelete = { /* TODO in a later phase */ }
                        )
                    }
                }
            }
        }
    }
}

