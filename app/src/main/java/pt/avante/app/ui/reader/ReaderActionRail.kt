package pt.avante.app.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.TextPrimary
import pt.avante.app.viewmodel.ReaderMode
import pt.avante.app.ui.layout.LocalAppViewport
import pt.avante.app.theme.Spacing

@Composable
fun ReaderActionRail(
    readerMode: ReaderMode,
    onBackClick: () -> Unit,
    onToggleAnnotationMode: () -> Unit,
    onCreateNoteClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompactHeight = LocalAppViewport.current.isCompactHeight
    val spacing = if (isCompactHeight) Spacing.m else Spacing.l

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = spacing, horizontal = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.padding(bottom = spacing)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = TextPrimary
            )
        }

        FloatingActionButton(
            onClick = onCreateNoteClick,
            containerColor = OriginalRed,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = "Criar nota")
        }

        FloatingActionButton(
            onClick = onToggleAnnotationMode,
            containerColor = if (readerMode == ReaderMode.ANNOTATION_MODE) OriginalRed else Color.White,
            contentColor = if (readerMode == ReaderMode.ANNOTATION_MODE) Color.White else TextPrimary,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Default.Draw, contentDescription = "Modo de anotação")
        }

        FloatingActionButton(
            onClick = onShareClick,
            containerColor = Color(0xFF4A7C77),
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = "Partilhar", modifier = Modifier.size(20.dp))
        }
    }
}
