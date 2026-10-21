package pt.avante.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pt.avante.app.data.ReaderTheme
import pt.avante.app.data.paleta
import pt.avante.app.theme.Cores
import pt.avante.app.theme.Spacing

private val Amostra = 44.dp
private val Visto = 20.dp
private val SeloDoVisto = 26.dp
private val ContornoEscolhido = 2.5.dp
private val Contorno = 1.dp

/** Onde a amostra da Automática passa do Papel ao Escuro: ao meio, sem degradê. */
private const val METADE = 0.5f

/**
 * Aparência (#8): as cores da app inteira, não só do artigo. Cada amostra mostra o fundo e o
 * acento da sua Aparência; a da Automática (#5) mostra as duas que ela alterna, Papel e Escuro.
 *
 * As cinco repartem a linha em partes iguais, sem intervalo entre elas: a folga entre amostras
 * vem da largura de cada parte, que a 360dp é 62dp, acima do alvo mínimo ([AlvoMinimo]). Um
 * intervalo fixo deixava a última com o que sobrava. O rótulo fica numa linha e, se for mais
 * largo do que a parte, sai por igual para os dois lados, sobre a folga entre amostras.
 */
@Composable
internal fun AparenciaOpcoes(
    selecionada: ReaderTheme,
    onSelect: (ReaderTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth().selectableGroup()) {
        ReaderTheme.entries.forEach { aparencia ->
            val selected = aparencia == selecionada
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .selectable(selected = selected, onClick = { onSelect(aparencia) }, role = Role.RadioButton)
            ) {
                Box(
                    modifier = Modifier
                        .size(Amostra)
                        .background(fundoDaAmostra(aparencia), CircleShape)
                        .border(
                            width = if (selected) ContornoEscolhido else Contorno,
                            color = if (selected) Cores.acento else Cores.divisor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Visto(aparencia)
                }
                Spacer(modifier = Modifier.height(Spacing.xxs))
                Text(
                    text = aparencia.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Cores.acento else Cores.textoSecundario,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.wrapContentWidth(unbounded = true)
                )
            }
        }
    }
}

/** A Automática é meio Papel, meio Escuro; as outras, o seu próprio fundo. */
private fun fundoDaAmostra(aparencia: ReaderTheme): Brush {
    val dia = Color(paleta(aparencia, sistemaEscuro = false).fundo)
    val noite = Color(paleta(aparencia, sistemaEscuro = true).fundo)
    return Brush.horizontalGradient(0f to dia, METADE to dia, METADE to noite, 1f to noite)
}

/**
 * O visto na cor do acento da amostra. Sobre a amostra dividida nenhum acento contrasta com as
 * duas metades, por isso o visto da Automática vai num selo do acento atual.
 */
@Composable
private fun Visto(aparencia: ReaderTheme) {
    if (aparencia == ReaderTheme.AUTOMATIC) {
        Box(
            modifier = Modifier.size(SeloDoVisto).background(Cores.acento, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Cores.sobreAcento,
                modifier = Modifier.size(Visto)
            )
        }
    } else {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = Color(paleta(aparencia).acento),
            modifier = Modifier.size(Visto)
        )
    }
}
