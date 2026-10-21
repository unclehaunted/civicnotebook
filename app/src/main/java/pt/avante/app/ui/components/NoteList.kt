package pt.avante.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.NoteType
import pt.avante.app.theme.AnnotationYellow
import pt.avante.app.theme.MutedGreen
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.TextPrimary
import pt.avante.app.theme.TextSecondary
import pt.avante.app.theme.WarmPaper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A lista de Notas, a mesma no Caderno e na Pesquisa: margens laterais de 16,
 * 12 entre cartões e o menu Editar/Eliminar em cada cartão. Quem a usa decide o
 * que é eliminar (a eliminação com "Anular" de [rememberDeleteNoteWithUndo]).
 */
@Composable
fun NoteList(
    notes: List<NoteEntity>,
    onOpen: (noteId: String) -> Unit,
    onDelete: (noteId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        items(notes, key = { it.noteId }) { note ->
            NoteCard(
                note = note,
                onClick = { onOpen(note.noteId) },
                onDelete = { onDelete(note.noteId) }
            )
        }
    }
}

/** Etiqueta de teste da barra vermelha do Excerto; não tem semântica própria. */
internal const val EXCERPT_BAR_TAG = "excerto-barra"

@Composable
internal fun NoteCard(note: NoteEntity, onClick: () -> Unit, onDelete: () -> Unit) {
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
        shape = MaterialTheme.shapes.medium,
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(Spacing.m)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (pillText, pillColor) = when (note.noteType) {
                        NoteType.STANDALONE -> "Livre" to Color(0xFF2196F3) // Blue
                        NoteType.ARTICLE_NOTE -> "Artigo" to MutedGreen // Green
                        NoteType.EXCERPT_NOTE -> "Excerto" to AnnotationYellow // Amber
                        NoteType.REFLECTION -> "Reflexão" to Color(0xFF9C27B0) // Purple
                        NoteType.COMMENTARY -> "Comentário" to OriginalRed // Red
                    }

                    Pill(text = pillText, containerColor = pillColor.copy(alpha = 0.1f), contentColor = pillColor)
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "•  $dateString",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Box {
                    // Alvo de toque de 48dp no próprio nó; o ícone visível fica a 20dp.
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
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

            Spacer(modifier = Modifier.height(Spacing.s))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            if (previewExcerpt != null) {
                Surface(
                    color = WarmPaper,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xs)
                ) {
                    // A altura intrínseca da linha é a do texto; a barra estica até ela.
                    Row(modifier = Modifier.padding(Spacing.s).height(IntrinsicSize.Min)) {
                        Box(
                            modifier = Modifier.width(4.dp).fillMaxHeight().background(OriginalRed)
                                .testTag(EXCERPT_BAR_TAG)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
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
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
            }

            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items(tags) { tag ->
                        Pill(text = tag, containerColor = WarmPaper, contentColor = TextSecondary)
                    }
                }
            }
        }
    }
}
