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
import pt.avante.app.theme.Cores

/**
 * Espaçamento (#12): Compacto, Normal e Arejado, lado a lado como o Tamanho.
 */
@Composable
internal fun EspacamentoOpcoes(
    selecionado: ReaderSpacing,
    onSelect: (ReaderSpacing) -> Unit,
    modifier: Modifier = Modifier
) = OpcoesSegmentadas(ReaderSpacing.entries, selecionado, ReaderSpacing::label, onSelect, modifier)

/**
 * Uma escolha das Definições em segmentos lado a lado (o Tamanho, o Espaçamento), com as
 * mesmas cores em todas. Cada segmento tem o alvo de toque mínimo de 48dp do Material.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> OpcoesSegmentadas(
    opcoes: List<T>,
    selecionada: T,
    rotulo: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        opcoes.forEachIndexed { index, opcao ->
            SegmentedButton(
                selected = opcao == selecionada,
                onClick = { onSelect(opcao) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = opcoes.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Cores.acento.copy(alpha = 0.1f),
                    activeContentColor = Cores.acento,
                    inactiveContentColor = Cores.textoSecundario
                )
            ) {
                Text(rotulo(opcao), style = MaterialTheme.typography.labelLarge, maxLines = 1)
            }
        }
    }
}
