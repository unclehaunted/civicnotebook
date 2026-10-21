package pt.avante.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pt.avante.app.data.Escrita
import pt.avante.app.data.TiposDeLetra
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.TextPrimary
import pt.avante.app.theme.TextSecondary
import pt.avante.app.theme.readerFontFamily

/** Alvo de toque mínimo de cada opção (48dp), com o rádio desenhado mais pequeno. */
private val AlvoMinimo = 48.dp

/**
 * Tipos de letra (#10): cada opção mostra o nome na sua face de display e, por baixo,
 * as duas faces que combina escritas na face de texto, para o Leitor ver a textura.
 */
@Composable
internal fun TiposDeLetraOpcoes(
    selecionados: TiposDeLetra,
    onSelect: (TiposDeLetra) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.selectableGroup()) {
        TiposDeLetra.entries.forEach { opcao ->
            OpcaoUnica(selected = opcao == selecionados, onClick = { onSelect(opcao) }) {
                Text(
                    text = opcao.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = readerFontFamily(opcao.display),
                    color = TextPrimary
                )
                Text(
                    text = faces(opcao),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = readerFontFamily(opcao.texto),
                    color = TextSecondary
                )
            }
        }
    }
}

/** Escrita (#10): a face das Notas do Leitor, cada opção escrita na própria face. */
@Composable
internal fun EscritaOpcoes(
    selecionada: Escrita,
    onSelect: (Escrita) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.selectableGroup()) {
        Escrita.entries.forEach { opcao ->
            OpcaoUnica(selected = opcao == selecionada, onClick = { onSelect(opcao) }) {
                Text(
                    text = opcao.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = readerFontFamily(opcao.face),
                    color = TextPrimary
                )
            }
        }
    }
}

private fun faces(opcao: TiposDeLetra): String =
    if (opcao.display == opcao.texto) opcao.texto.label else "${opcao.display.label} e ${opcao.texto.label}"

/** Uma linha de escolha única: a linha inteira é o alvo de toque e leva o estado ao leitor de ecrã. */
@Composable
private fun OpcaoUnica(selected: Boolean, onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = AlvoMinimo)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = OriginalRed)
        )
        Spacer(Modifier.width(Spacing.s))
        Column(content = content)
    }
}
