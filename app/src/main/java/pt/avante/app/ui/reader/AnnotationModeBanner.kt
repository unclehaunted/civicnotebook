package pt.avante.app.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.Cores
import pt.avante.app.theme.Spacing

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
            color = Cores.superficie,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, Cores.acento),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Modo de Anotação Activo",
                    style = MaterialTheme.typography.labelLarge,
                    color = Cores.acento,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Seleccione um excerto para criar uma nota",
                    style = MaterialTheme.typography.bodySmall,
                    color = Cores.textoSecundario
                )
            }
        }
    }
}
