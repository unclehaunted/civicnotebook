package pt.avante.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.Cores

/** Etiqueta da barra sem a margem dos insets, para os testes medirem onde começa. */
internal const val TITLE_BAR_TAG = "TitleBar"

/**
 * A barra de título de todos os ecrãs sem masthead (spec #49): Caderno, Pesquisa,
 * Arquivo, Definições, Sobre e editor de Nota. A Edição usa o masthead.
 *
 * O título fica à esquerda na cor de texto principal, nunca a vermelho (ADR 0001).
 * [onBack] só existe nos ecrãs completos e mostra o "Voltar" à esquerda.
 * A margem superior vem de [windowInsets], a barra de estado por omissão como nas
 * barras do Material 3. Dentro do conteúdo do `MainApp` esses insets já foram
 * consumidos pelo padding do Scaffold, por isso a margem não se soma duas vezes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets
) {
    Column(
        modifier = modifier
            .background(Cores.fundo)
            .windowInsetsPadding(windowInsets)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Cores.textoPrimario
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = Cores.textoSecundario
                        )
                    }
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            },
            actions = actions,
            windowInsets = WindowInsets(0.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Cores.fundo,
                titleContentColor = Cores.textoPrimario,
                navigationIconContentColor = Cores.textoPrimario,
                actionIconContentColor = Cores.textoPrimario
            ),
            modifier = Modifier.testTag(TITLE_BAR_TAG)
        )
    }
}
