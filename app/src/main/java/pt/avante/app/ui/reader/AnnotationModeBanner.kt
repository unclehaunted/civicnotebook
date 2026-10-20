package pt.avante.app.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.TextSecondary

/** Faixa que avisa que o Modo de Anotação está ativo, igual nos layouts compacto e largo. */
@Composable
fun AnnotationModeBanner(
    visible: Boolean,
    topOffset: Dp,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier.padding(top = topOffset)
    ) {
        Surface(
            color = Color(0xFFFDECEA),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OriginalRed),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Modo de Anotação Activo",
                    style = MaterialTheme.typography.labelLarge,
                    color = OriginalRed,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Seleccione um excerto para criar uma nota",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
