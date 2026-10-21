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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.avante.app.theme.Cores
import pt.avante.app.viewmodel.NoteEditorUiState
import pt.avante.app.viewmodel.NoteViewModel
import pt.avante.app.theme.ExcertosDoJornal
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.tipografiaDoArtigo
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
    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId, isNewNote)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Cores.fundo
    ) {
        pt.avante.app.ui.layout.AdaptiveContent(maxWidth = 960.dp) {
            when (val state = uiState) {
                is NoteEditorUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Cores.acento)
                    }
                }
                is NoteEditorUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = Cores.acento)
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
                                        Text("Ver Artigo", color = Cores.acento)
                                    }
                                }
                            }
                        )
    
                        HorizontalDivider(color = Cores.divisor)
    
                        NoteEditorFields(
                            title = note.title,
                            body = note.bodyMarkdown,
                            onTitleChange = viewModel::updateTitle,
                            onBodyChange = viewModel::updateBody
                        )
                    }
                }
            }
        }
    }
}

/** O título, a barra de markdown e o corpo de uma Nota, sem estado: o ecrã liga-os ao ViewModel. */
@Composable
internal fun NoteEditorFields(
    title: String,
    body: String,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit
) {
    // O editor segue o Tamanho e o Espaçamento, como o artigo.
    val tipografia = tipografiaDoArtigo()
    // O título da Nota: a face de display dos Tipos de letra, como os títulos do artigo, um
    // degrau abaixo do título de um artigo.
    val estiloDoTitulo = tipografia.tituloDaNota
    // As palavras do Leitor: a face da Escrita, no corpo e na entrelinha do Tamanho e do Espaçamento.
    val estiloDoCorpo = tipografia.corpoDaNota
    val largura = larguraDoCampo(tipografia.espacamento.columnMaxWidthSp)
    val semFundo = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent
    )
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = title,
            onValueChange = onTitleChange,
            placeholder = { Text("Título da Nota", style = estiloDoTitulo) },
            modifier = Modifier
                .padding(horizontal = Spacing.m, vertical = Spacing.xs)
                .widthIn(max = largura)
                .fillMaxWidth(),
            colors = semFundo,
            textStyle = estiloDoTitulo.copy(color = Cores.textoPrimario)
        )
        HorizontalDivider(color = Cores.divisor, modifier = Modifier.padding(horizontal = Spacing.m))
        BarraDeMarkdown(onInsert = { onBodyChange(body + it) })
        HorizontalDivider(color = Cores.divisor)
        TextField(
            value = body,
            onValueChange = onBodyChange,
            placeholder = {
                Text("Comece a escrever em Markdown...", style = estiloDoCorpo, color = Cores.textoSecundario)
            },
            modifier = Modifier.padding(Spacing.m).widthIn(max = largura).fillMaxSize(),
            colors = semFundo,
            textStyle = estiloDoCorpo.copy(color = Cores.escrita),
            // As palavras do jornal (`>`): o itálico da face de display, com a marca à vista.
            visualTransformation = ExcertosDoJornal(tipografia.excerto, Cores.textoSecundario)
        )
    }
}

/** Largura de um campo cujo texto para nos [colunaSp] do artigo: soma-lhe o recuo interno do campo. */
@Composable
private fun larguraDoCampo(colunaSp: Float): Dp {
    val recuo = TextFieldDefaults.contentPaddingWithoutLabel()
    return with(LocalDensity.current) { colunaSp.sp.toDp() } +
        recuo.calculateLeftPadding(LayoutDirection.Ltr) +
        recuo.calculateRightPadding(LayoutDirection.Ltr)
}

/** Botões que acrescentam markdown ao fim do corpo. */
@Composable
private fun BarraDeMarkdown(onInsert: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Cores.superficie)
            .padding(horizontal = Spacing.xs, vertical = Spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
    ) {
        IconButton(onClick = { onInsert("**Negrito**") }) {
            Icon(Icons.Default.FormatBold, contentDescription = "Negrito")
        }
        IconButton(onClick = { onInsert("*Itálico*") }) {
            Icon(Icons.Default.FormatItalic, contentDescription = "Itálico")
        }
        IconButton(onClick = { onInsert("\n> Citação") }) {
            Icon(Icons.Default.FormatQuote, contentDescription = "Citação")
        }
    }
}
