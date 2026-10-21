package pt.avante.app.ui.reader

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import pt.avante.app.theme.Cores
import pt.avante.app.theme.SobreImagem
import pt.avante.app.viewmodel.ReaderMode
import pt.avante.app.theme.Spacing

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
    val buttonBorderModifier =
        if (isRolledUp) Modifier.border(1.dp, Cores.acento.copy(alpha = 0.5f), CircleShape) else Modifier

    // Back Button
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.xxl, start = Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            // Pousado sobre a fotografia do artigo.
            modifier = Modifier.background(SobreImagem.veu.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = SobreImagem.texto)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Floating Top Right Buttons (Annotation / Note)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = Spacing.xxl, end = Spacing.m),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            if (readerMode == ReaderMode.READ_MODE) {
                IconButton(
                    onClick = onCreateNoteClick,
                    modifier = Modifier.size(48.dp).then(buttonBorderModifier).background(Cores.cartao, CircleShape)
                ) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "Criar nota do artigo", tint = Cores.acento)
                }
                IconButton(
                    onClick = onToggleAnnotationMode,
                    modifier = Modifier.size(48.dp).then(buttonBorderModifier).background(Cores.cartao, CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Ativar Modo de Anotação", tint = Cores.acento)
                }
            } else {
                // Annotation mode active - show exit button with stronger visual
                Surface(
                    color = Cores.acento,
                    shape = CircleShape,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(48.dp).clickable { onToggleAnnotationMode() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Sair do Modo de Anotação",
                            tint = Cores.sobreAcento,
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
                            colors = listOf(Color.Transparent, Cores.fundo)
                        )
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .padding(bottom = Spacing.xl)
                        .size(48.dp)
                        // Neutro: o vermelho é o único acento (ADR 0001).
                        .background(Cores.cartao, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partilhar",
                        tint = Cores.textoPrimario,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
