package pt.avante.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.avante.app.data.db.HighlightEntity
import pt.avante.app.theme.*
import pt.avante.app.ui.components.AnnotationActionRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderAnnotationSheets(
    selectedPassageText: String?,
    onDismissSelection: () -> Unit,
    onHighlightSelection: () -> Unit,
    onCreateNoteFromSelection: () -> Unit,
    
    // Bottom sheet for selecting an existing highlight
    selectedHighlight: HighlightEntity?,
    onDismissHighlight: () -> Unit,
    onCreateNoteFromHighlight: () -> Unit,
    onDeleteHighlight: () -> Unit
) {
    // Context Menu Bottom Sheet
    if (selectedPassageText != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissSelection,
            containerColor = Cores.cartao
        ) {
            Column(modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(max = 600.dp).padding(Spacing.m).fillMaxWidth()) {
                Text(
                    text = "Ação de Anotação",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = Spacing.m)
                )
                AnnotationActionRow(
                    icon = Icons.Default.BorderColor,
                    title = "Realçar excerto",
                    subtitle = "Guardar este trecho no artigo",
                    // O amarelo do Destaque só aparece onde se destaca (ADR 0001).
                    tint = Cores.textoPrimario,
                    background = Cores.destaque,
                    onClick = onHighlightSelection
                )
                
                AnnotationActionRow(
                    icon = Icons.Default.NoteAdd,
                    title = "Criar nota",
                    subtitle = "Abrir uma nova nota com este excerto",
                    tint = Cores.acento,
                    background = Cores.superficie,
                    onClick = onCreateNoteFromSelection
                )

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }

    // Existing Highlight details sheet
    if (selectedHighlight != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissHighlight,
            containerColor = Cores.cartao
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xl, top = Spacing.xs, start = Spacing.l, end = Spacing.l)
            ) {
                Text(
                    "O que quer fazer com este excerto?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Cores.textoPrimario,
                    modifier = Modifier.padding(bottom = Spacing.m)
                )

                Surface(
                    color = Cores.fundo,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.l)
                ) {
                    Row(modifier = Modifier.padding(Spacing.s)) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(48.dp)
                                .background(Cores.acento)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "\"${selectedHighlight.selectedText}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Cores.textoSecundario,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                ListItem(
                    headlineContent = { Text("Criar nota", color = Cores.textoPrimario) },
                    leadingContent = { Icon(Icons.Default.NoteAdd, contentDescription = null, tint = Cores.acento) },
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onCreateNoteFromHighlight() }
                )
                ListItem(
                    headlineContent = { Text("Remover realce", color = Cores.textoPrimario) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = Cores.acento) },
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onDeleteHighlight() }
                )
            }
        }
    }
}
