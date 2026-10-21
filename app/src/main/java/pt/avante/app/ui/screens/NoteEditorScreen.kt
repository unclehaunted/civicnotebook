package pt.avante.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import pt.avante.app.theme.WarmPaper
import pt.avante.app.viewmodel.NoteEditorUiState
import pt.avante.app.viewmodel.NoteViewModel
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.escalaDoArtigo
import pt.avante.app.theme.naEscalaDoArtigo
import pt.avante.app.ui.settings.LocalReaderSettings
import pt.avante.app.ui.components.TitleBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String,
    isNewNote: Boolean,
    viewModel: NoteViewModel,
    onBackClick: () -> Unit,
    onOpenArticle: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    // O editor segue o Tamanho como o artigo; o título da Nota fica um degrau abaixo do de um artigo.
    val escala = escalaDoArtigo(LocalReaderSettings.current.fontSize)
    val estiloDoTitulo = MaterialTheme.typography.titleLarge
        .naEscalaDoArtigo(escala.tituloDaNota)
        .copy(fontWeight = FontWeight.Bold)

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId, isNewNote)
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
                        TitleBar(
                            title = "Editar Nota",
                            subtitle = if (isSaving) "A guardar..." else null,
                            onBack = onBackClick,
                            actions = {
                                if (state.articleSourceId != null) {
                                    TextButton(onClick = { onOpenArticle(state.articleSourceId) }) {
                                        Text("Ver Artigo", color = OriginalRed)
                                    }
                                }
                            }
                        )
    
                        HorizontalDivider(color = SoftDivider)
    
                        // Title Field
                        TextField(
                            value = note.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            placeholder = { Text("Título da Nota", style = estiloDoTitulo) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.m, vertical = Spacing.xs),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = estiloDoTitulo.copy(color = TextPrimary)
                        )
    
                        HorizontalDivider(color = SoftDivider, modifier = Modifier.padding(horizontal = Spacing.m))
    
                        // Markdown Toolbar (Simple for now)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = Spacing.xs, vertical = Spacing.xxs),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
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
                            modifier = Modifier.fillMaxSize().padding(Spacing.m),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                                .naEscalaDoArtigo(escala.corpo)
                                .copy(color = TextPrimary)
                        )
                    }
                }
            }
        }
    }
}
