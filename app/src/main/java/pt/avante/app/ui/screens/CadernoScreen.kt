package pt.avante.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.background
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.theme.*
import pt.avante.app.viewmodel.CadernoUiState
import pt.avante.app.viewmodel.CadernoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadernoScreen(
    viewModel: CadernoViewModel,
    onNoteClick: (NoteEntity) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }

    Scaffold(
        containerColor = WarmPaper,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val noteId = viewModel.createNewNote()
                    if (noteId != null) {
                        onNoteClick(NoteEntity(
                            noteId = noteId,
                            title = "Nova Nota",
                            bodyMarkdown = "",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            noteType = pt.avante.app.data.db.NoteType.STANDALONE
                        ))
                    }
                },
                containerColor = OriginalRed,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Nota")
            }
        }
    ) { paddingValues ->
        pt.avante.app.ui.layout.AdaptiveContent(maxWidth = 960.dp) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "Caderno", 
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = OriginalRed
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmPaper),
                )
                
                HorizontalDivider(color = SoftDivider)

                when (val state = uiState) {
                    is CadernoUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = OriginalRed)
                        }
                    }
                    is CadernoUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "O seu caderno está vazio.\nCrie notas a partir dos artigos.",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    is CadernoUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.notes, key = { it.noteId }) { note ->
                                NoteCard(
                                    note = note,
                                    onClick = { onNoteClick(note) },
                                    onDelete = { viewModel.removeNote(note.noteId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteCard(note: NoteEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateString = dateFormat.format(Date(note.updatedAt))
    
    var expanded by remember { mutableStateOf(false) }

    // Parsing for preview
    val tags = Regex("""#[\p{L}\p{N}_-]+""").findAll(note.bodyMarkdown).map { it.value }.distinct().toList()
    val lines = note.bodyMarkdown.split("\n")
    val excerptLines = lines.filter { it.startsWith(">") }.map { it.removePrefix(">").trim() }
    val reflectionLines = lines.filter { !it.startsWith(">") && !it.startsWith("#") && it.isNotBlank() }
    
    val previewExcerpt = excerptLines.firstOrNull()
    val previewReflection = reflectionLines.firstOrNull()

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (pillText, pillColor) = when (note.noteType) {
                        pt.avante.app.data.db.NoteType.STANDALONE -> "Livre" to Color(0xFF2196F3) // Blue
                        pt.avante.app.data.db.NoteType.ARTICLE_NOTE -> "Artigo" to MutedGreen // Green
                        pt.avante.app.data.db.NoteType.EXCERPT_NOTE -> "Excerto" to AnnotationYellow // Amber
                        pt.avante.app.data.db.NoteType.REFLECTION -> "Reflexão" to Color(0xFF9C27B0) // Purple
                        pt.avante.app.data.db.NoteType.COMMENTARY -> "Comentário" to OriginalRed // Red
                    }
                    
                    Surface(
                        color = pillColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = pillText,
                            style = MaterialTheme.typography.labelSmall,
                            color = pillColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  $dateString",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Box {
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar", color = TextPrimary) },
                            onClick = { expanded = false; onClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = OriginalRed) },
                            onClick = { expanded = false; onDelete() }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            if (previewExcerpt != null) {
                Surface(
                    color = WarmPaper,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Box(modifier = Modifier.width(4.dp).height(48.dp).background(OriginalRed))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\"$previewExcerpt\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (previewReflection != null) {
                Text(
                    text = previewReflection,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tags) { tag ->
                        Surface(
                            color = WarmPaper,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
