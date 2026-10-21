package pt.avante.app.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pt.avante.app.data.ReaderSpacing
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.TextSecondary

/**
 * Espaçamento (#12): Compacto, Normal e Arejado, lado a lado como o Tamanho. Cada segmento
 * tem o alvo de toque mínimo de 48dp do Material.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EspacamentoOpcoes(
    selecionado: ReaderSpacing,
    onSelect: (ReaderSpacing) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        ReaderSpacing.entries.forEachIndexed { index, opcao ->
            SegmentedButton(
                selected = opcao == selecionado,
                onClick = { onSelect(opcao) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = ReaderSpacing.entries.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = OriginalRed.copy(alpha = 0.1f),
                    activeContentColor = OriginalRed,
                    inactiveContentColor = TextSecondary
                )
            ) {
                Text(opcao.label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }
    }
}
