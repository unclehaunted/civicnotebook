package pt.avante.app.ui.reader

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pt.avante.app.data.Article
import pt.avante.app.theme.OriginalRed
import pt.avante.app.ui.settings.LocalReaderSettings
import pt.avante.app.viewmodel.ReaderMode

@Composable
fun ReaderFloatingActions(
    article: Article?,
    articleUrl: String,
    readerMode: ReaderMode,
    isRolledUp: Boolean,
    showShareFab: Boolean,
    onBackClick: () -> Unit,
    onToggleAnnotationMode: () -> Unit,
    onCreateNoteClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val buttonBorderModifier = if (isRolledUp) Modifier.border(1.dp, OriginalRed.copy(alpha = 0.5f), CircleShape) else Modifier

    // Back Button
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Floating Top Right Buttons (Annotation / Note)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (readerMode == ReaderMode.READ_MODE) {
                IconButton(
                    onClick = onCreateNoteClick,
                    modifier = Modifier.size(48.dp).then(buttonBorderModifier).background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "Criar nota do artigo", tint = OriginalRed)
                }
                IconButton(
                    onClick = onToggleAnnotationMode,
                    modifier = Modifier.size(48.dp).then(buttonBorderModifier).background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Ativar Modo de Anotação", tint = OriginalRed)
                }
            } else {
                // Annotation mode active - show exit button with stronger visual
                Surface(
                    color = OriginalRed,
                    shape = CircleShape,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(48.dp).clickable { onToggleAnnotationMode() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Sair do Modo de Anotação",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        // Share FAB & Bottom Gradient Fade - Only visible when rolled up
        AnimatedVisibility(
            visible = showShareFab,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(LocalReaderSettings.current.theme.backgroundColor))
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .size(48.dp)
                        .background(Color(0xFF4A7C77), CircleShape) // Muted blue-green
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, 
                        contentDescription = "Partilhar", 
                        tint = Color.White, 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
