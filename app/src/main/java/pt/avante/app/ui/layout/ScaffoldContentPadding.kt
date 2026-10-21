package pt.avante.app.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier

/**
 * Afasta o conteúdo pelo padding do Scaffold e marca-o como consumido, para que
 * os insets lidos lá dentro (a barra de estado da [pt.avante.app.ui.components.TitleBar],
 * os de um Scaffold aninhado) não somem a mesma margem outra vez.
 */
fun Modifier.scaffoldContentPadding(innerPadding: PaddingValues): Modifier =
    padding(innerPadding).consumeWindowInsets(innerPadding)
