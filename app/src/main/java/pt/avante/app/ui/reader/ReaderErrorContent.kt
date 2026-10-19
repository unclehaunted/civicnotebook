package pt.avante.app.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.OriginalRed
import pt.avante.app.ui.settings.LocalReaderSettings
import pt.avante.app.viewmodel.ReaderErrorReason

/** Título e explicação, em português, de cada motivo de falha. "Offline" só para rede/não guardado. */
internal fun ReaderErrorReason.title(): String = when (this) {
    ReaderErrorReason.NO_NETWORK,
    ReaderErrorReason.NOT_CACHED -> "Artigo indisponível offline"
    ReaderErrorReason.TIMEOUT -> "O servidor demorou a responder"
    ReaderErrorReason.SERVER -> "Não foi possível carregar o artigo"
}

internal fun ReaderErrorReason.message(): String = when (this) {
    ReaderErrorReason.NO_NETWORK ->
        "Não há ligação à Internet neste momento.\nLigue-se para abrir o artigo."
    ReaderErrorReason.NOT_CACHED ->
        "Este artigo ainda não foi guardado neste dispositivo.\nLigue-se à Internet para o abrir."
    ReaderErrorReason.TIMEOUT ->
        "A ligação está lenta ou instável.\nTente de novo dentro de momentos."
    ReaderErrorReason.SERVER ->
        "Ocorreu um problema ao obter o artigo.\nTente de novo ou volte mais tarde."
}

@Composable
fun ReaderErrorContent(
    reason: ReaderErrorReason,
    onRetry: () -> Unit,
    onBackClick: () -> Unit
) {
    val readerTheme = LocalReaderSettings.current.theme
    val readerBackground = Color(readerTheme.backgroundColor)
    val readerTextPrimary = Color(readerTheme.textColor)
    val readerTextSecondary = Color(readerTheme.secondaryTextColor)

    Surface(modifier = Modifier.fillMaxSize(), color = readerBackground) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = reason.title(),
                    style = MaterialTheme.typography.titleLarge,
                    color = readerTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = reason.message(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = readerTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onBackClick) {
                        Text("Voltar", color = readerTextPrimary)
                    }
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = OriginalRed)
                    ) {
                        Text("Tentar de novo", color = Color.White)
                    }
                }
            }
        }
    }
}
