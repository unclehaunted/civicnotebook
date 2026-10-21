package pt.avante.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import pt.avante.app.theme.Spacing

/**
 * Um texto curto numa pílula, numa só linha: o tipo e as etiquetas de uma Nota,
 * o autor e a rubrica de um artigo. Quem a usa escolhe as cores; a forma, o
 * estilo e as margens são os mesmos em todo o lado, salvo o que se passa aqui.
 */
@Composable
fun Pill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall,
    horizontalPadding: Dp = Spacing.xs,
    fontWeight: FontWeight? = null
) {
    Surface(color = containerColor, shape = shape, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = fontWeight),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = Spacing.xxs)
        )
    }
}
