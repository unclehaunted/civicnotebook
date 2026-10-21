package pt.avante.app.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.Spacing

@Composable
fun CategoryRubricPill(
    formattedTaxonomy: String?,
    modifier: Modifier = Modifier
) {
    if (formattedTaxonomy.isNullOrBlank()) return

    Pill(
        text = formattedTaxonomy,
        containerColor = OriginalRed,
        contentColor = Color.White,
        modifier = modifier,
        shape = CircleShape,
        horizontalPadding = Spacing.s,
        fontWeight = FontWeight.Bold
    )
}
