package pt.avante.app.ui.reader

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.SoftDivider

@Composable
fun ReaderEndMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(), 
        contentAlignment = Alignment.Center
    ) {
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(0.67f).padding(vertical = 32.dp),
            color = SoftDivider,
            thickness = 1.dp
        )
    }
}
