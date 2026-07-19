package pt.avante.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.SoftDivider
import pt.avante.app.theme.TextPrimary
import pt.avante.app.theme.TextSecondary
import pt.avante.app.theme.WarmPaper
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import pt.avante.app.viewmodel.NoteEditorUiState
import pt.avante.app.viewmodel.NoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String,
    viewModel: NoteViewModel,
    onBackClick: () -> Unit,
    onOpenArticle: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarmPaper
    ) {
        pt.avante.app.ui.layout.AdaptiveContent(maxWidth = 960.dp) {
            when (val state = uiState) {
                is NoteEditorUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OriginalRed)
                    }
                }
                is NoteEditorUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = OriginalRed)
                    }
                }
                is NoteEditorUiState.Success -> {
                    val note = state.note
                    Column(modifier = Modifier.fillMaxSize()) {
                        TopAppBar(
                            title = { 
                                Column {
                                    Text("Editar Nota", color = TextPrimary)
                                    if (isSaving) {
                                        Text(
                                            text = "A guardar...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = onBackClick) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                                }
                            },
                            actions = {
                                if (state.articleSourceId != null) {
                                    Surface(
                                        color = OriginalRed.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.padding(end = 16.dp).clickable { onOpenArticle(state.articleSourceId) }
                                    ) {
                                        Text(
                                            "Ver Artigo", 
                                            color = OriginalRed, 
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmPaper)
                        )
    
                        HorizontalDivider(color = SoftDivider)
    
                        // Title Field
                        TextField(
                            value = note.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            placeholder = { Text("Título da Nota", style = MaterialTheme.typography.titleLarge) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
    
                        HorizontalDivider(color = SoftDivider, modifier = Modifier.padding(horizontal = 16.dp))
    
                        // Markdown Toolbar (Simple for now)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { viewModel.updateBody(note.bodyMarkdown + "**Negrito**") }) {
                                Icon(Icons.Default.FormatBold, contentDescription = "Negrito")
                            }
                            IconButton(onClick = { viewModel.updateBody(note.bodyMarkdown + "*Itálico*") }) {
                                Icon(Icons.Default.FormatItalic, contentDescription = "Itálico")
                            }
                            IconButton(onClick = { viewModel.updateBody(note.bodyMarkdown + "\n> Citação") }) {
                                Icon(Icons.Default.FormatQuote, contentDescription = "Citação")
                            }
                        }
    
                        HorizontalDivider(color = SoftDivider)
    
                        // Body Field
                        TextField(
                            value = note.bodyMarkdown,
                            onValueChange = { viewModel.updateBody(it) },
                            placeholder = { Text("Comece a escrever em Markdown...") },
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
                        )
                    }
                }
            }
        }
    }
}
