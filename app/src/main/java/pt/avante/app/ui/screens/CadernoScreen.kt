package pt.avante.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pt.avante.app.theme.*
import pt.avante.app.ui.components.NoteList
import pt.avante.app.ui.components.TitleBar
import pt.avante.app.ui.components.rememberDeleteNoteWithUndo
import pt.avante.app.viewmodel.CadernoUiState
import pt.avante.app.viewmodel.CadernoViewModel

@Composable
fun CadernoScreen(
    viewModel: CadernoViewModel,
    onNoteClick: (String) -> Unit,
    onNewNote: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var isCreatingNote by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val deleteNote = rememberDeleteNoteWithUndo(snackbarHostState, viewModel::removeNote, viewModel::restoreNote)

    Scaffold(
        containerColor = WarmPaper,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Um segundo toque antes de o editor abrir criaria uma Nota que nunca se abre.
                    if (!isCreatingNote) {
                        isCreatingNote = true
                        scope.launch {
                            try {
                                onNewNote(viewModel.createNewNote())
                            } finally {
                                isCreatingNote = false
                            }
                        }
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
                TitleBar(title = "Caderno")

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
                        NoteList(notes = state.notes, onOpen = onNoteClick, onDelete = deleteNote)
                    }
                }
            }
        }
    }
}
