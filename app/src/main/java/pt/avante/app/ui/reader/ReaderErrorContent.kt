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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.Cores
import pt.avante.app.viewmodel.ReaderErrorReason
import pt.avante.app.theme.Spacing

/** Título e explicação, em português, de cada motivo de falha. "Offline" só para rede/não guardado. */
internal fun ReaderErrorReason.title(): String = when (this) {
    ReaderErrorReason.NO_NETWORK,
    ReaderErrorReason.NOT_CACHED -> "Artigo indisponível offline"
    ReaderErrorReason.TIMEOUT -> "O servidor demorou a responder"
    ReaderErrorReason.SERVER -> "Não foi possível carregar o artigo"
    ReaderErrorReason.NO_TEXT -> "Texto ainda não publicado"
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
    ReaderErrorReason.NO_TEXT ->
        "O avante.pt tem, por agora, só o título deste artigo.\nTente de novo mais tarde."
}

@Composable
fun ReaderErrorContent(
    reason: ReaderErrorReason,
    onRetry: () -> Unit,
    onBackClick: () -> Unit
) {
    val readerBackground = Cores.fundo
    val readerTextPrimary = Cores.textoPrimario
    val readerTextSecondary = Cores.textoSecundario

    Surface(modifier = Modifier.fillMaxSize(), color = readerBackground) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(Spacing.l),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = reason.title(),
                    style = MaterialTheme.typography.titleLarge,
                    color = readerTextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Spacing.m))
                Text(
                    text = reason.message(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = readerTextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(Spacing.xl))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                    OutlinedButton(onClick = onBackClick) {
                        Text("Voltar", color = readerTextPrimary)
                    }
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Cores.acento)
                    ) {
                        Text("Tentar de novo", color = Cores.sobreAcento)
                    }
                }
            }
        }
    }
}
