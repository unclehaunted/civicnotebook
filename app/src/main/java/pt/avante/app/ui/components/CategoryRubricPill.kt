package pt.avante.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.OriginalRed

@Composable
fun CategoryRubricPill(
    formattedTaxonomy: String?, 
    modifier: Modifier = Modifier
) {
    if (formattedTaxonomy.isNullOrBlank()) return

    Surface(
        color = OriginalRed,
        shape = CircleShape,
        modifier = modifier
    ) {
        Text(
            text = formattedTaxonomy,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
