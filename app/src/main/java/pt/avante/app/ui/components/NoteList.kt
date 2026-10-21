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
import pt.avante.app.data.db.summary
import pt.avante.app.theme.Cores
import pt.avante.app.theme.Spacing
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

    val summary = remember(note.bodyMarkdown) { note.summary() }
    val excerpt = summary.excerpt
    val firstLine = summary.firstLine
    val tags = summary.tags

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = Cores.cartao,
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
                    // O tipo diz-se pelo rótulo; a pill é neutra para o amarelo do Destaque e o
                    // vermelho do acento guardarem o seu sentido (#8).
                    val pillText = when (note.noteType) {
                        NoteType.STANDALONE -> "Livre"
                        NoteType.ARTICLE_NOTE -> "Artigo"
                        NoteType.EXCERPT_NOTE -> "Excerto"
                        NoteType.REFLECTION -> "Reflexão"
                        NoteType.COMMENTARY -> "Comentário"
                    }

                    Pill(text = pillText, containerColor = Cores.superficie, contentColor = Cores.textoSecundario)
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "•  $dateString",
                        style = MaterialTheme.typography.labelMedium,
                        color = Cores.textoSecundario
                    )
                }
                Box {
                    // Alvo de toque de 48dp no próprio nó; o ícone visível fica a 20dp.
                    IconButton(onClick = { expanded = true }, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Mais opções",
                            tint = Cores.textoSecundario,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Cores.superficie)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar", color = Cores.textoPrimario) },
                            onClick = { expanded = false; onClick() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = Cores.acento) },
                            onClick = { expanded = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.s))

            Text(
                text = note.title,
                // O título da Nota: a face de display dos Tipos de letra, no tamanho do chrome.
                style = MaterialTheme.typography.titleMedium
                    .copy(fontFamily = DuasVozes.display, fontWeight = FontWeight.Bold),
                color = Cores.textoPrimario,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            if (excerpt != null) {
                Surface(
                    color = Cores.fundo,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xs)
                ) {
                    // A altura intrínseca da linha é a do texto; a barra estica até ela.
                    Row(modifier = Modifier.padding(Spacing.s).height(IntrinsicSize.Min)) {
                        Box(
                            modifier = Modifier.width(4.dp).fillMaxHeight().background(Cores.acento)
                                .testTag(EXCERPT_BAR_TAG)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = "\"$excerpt\"",
                            // As palavras do jornal: o itálico desenhado da face de display.
                            style = MaterialTheme.typography.bodyMedium.merge(DuasVozes.excerto),
                            color = Cores.textoSecundario,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (firstLine != null) {
                Text(
                    text = firstLine,
                    // As palavras do Leitor: a face e o papel de cor da Escrita.
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = DuasVozes.escrita),
                    color = Cores.escrita,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
            }

            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items(tags) { tag ->
                        Pill(text = tag, containerColor = Cores.fundo, contentColor = Cores.textoSecundario)
                    }
                }
            }
        }
    }
}
