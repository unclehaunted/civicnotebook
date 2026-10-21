package pt.avante.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import pt.avante.app.R
import pt.avante.app.data.avante.ProjectSite
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.TextPrimary
import pt.avante.app.theme.TextSecondary
import pt.avante.app.theme.WarmPaper

private const val INDEPENDENCE_NOTE =
    "Projeto independente, sem ligação oficial ao «Avante!». " +
        "Os artigos vêm de avante.pt e pertencem aos seus autores e ao «Avante!»."

/** Ecrã Sobre, aberto a partir do Menu: nome e versão da app, origem dos artigos, licença e código. */
@Composable
fun SobreScreen(
    versionName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize(), color = WarmPaper) {
        Column(modifier = Modifier.fillMaxSize()) {
            SobreHeader(onBackClick = onBackClick)
            SobreBody(versionName = versionName)
        }
    }
}

/** Cabeçalho simples; #51 troca-o pela barra de título comum. */
@Composable
private fun SobreHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = Spacing.xxs, end = Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = OriginalRed
            )
        }
        Text(
            text = "Sobre",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SobreBody(versionName: String) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.l, vertical = Spacing.m),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Column {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Versão $versionName",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        Text(
            text = INDEPENDENCE_NOTE,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Text(
            text = "Licença: GNU GPLv3",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Column {
            Text(
                text = "Código",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            TextButton(onClick = { uriHandler.openUri(ProjectSite.REPOSITORY_URL) }) {
                Text(
                    text = ProjectSite.REPOSITORY_URL.removePrefix("https://"),
                    color = OriginalRed
                )
            }
        }
    }
}
