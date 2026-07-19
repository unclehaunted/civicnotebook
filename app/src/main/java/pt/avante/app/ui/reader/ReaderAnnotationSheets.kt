package pt.avante.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(max = 600.dp).padding(16.dp).fillMaxWidth()) {
                Text(
                    text = "Ação de Anotação",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                AnnotationActionRow(
                    icon = Icons.Default.BorderColor,
                    title = "Realçar excerto",
                    subtitle = "Guardar este trecho no artigo",
                    tint = AnnotationYellow,
                    background = AnnotationYellow.copy(alpha = 0.10f),
                    onClick = onHighlightSelection
                )
                
                AnnotationActionRow(
                    icon = Icons.Default.NoteAdd,
                    title = "Criar nota",
                    subtitle = "Abrir uma nova nota com este excerto",
                    tint = MutedGreen,
                    background = MutedGreen.copy(alpha = 0.07f),
                    onClick = onCreateNoteFromSelection
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Existing Highlight details sheet
    if (selectedHighlight != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissHighlight,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    "O que quer fazer com este excerto?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Surface(
                    color = WarmPaper,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(48.dp)
                                .background(OriginalRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "\"${selectedHighlight.selectedText}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                ListItem(
                    headlineContent = { Text("Criar nota", color = TextPrimary) },
                    leadingContent = { Icon(Icons.Default.NoteAdd, contentDescription = null, tint = OriginalRed) },
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onCreateNoteFromHighlight() }
                )
                ListItem(
                    headlineContent = { Text("Remover realce", color = TextPrimary) },
                    leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = OriginalRed) },
                    colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onDeleteHighlight() }
                )
            }
        }
    }
}
