package pt.avante.app.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import pt.avante.app.theme.Spacing

/** Conteúdo do Menu aberto a partir da Edição. "Exportar notas" volta com #29. */
@Composable
fun MainMenu(
    onSubscription: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(Spacing.m).fillMaxWidth()) {
        Text(
            "Menu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Spacing.m)
        )
        ListItem(
            headlineContent = { Text("Assinatura") },
            leadingContent = { Icon(Icons.Default.Star, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onSubscription)
        )
        ListItem(
            headlineContent = { Text("Definições") },
            leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onSettings)
        )
        ListItem(
            headlineContent = { Text("Sobre") },
            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onAbout)
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}
