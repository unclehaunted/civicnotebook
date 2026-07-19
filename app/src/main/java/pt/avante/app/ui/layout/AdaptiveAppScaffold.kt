package pt.avante.app.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AdaptiveAppScaffold(
    bottomBar: @Composable () -> Unit = {},
    navigationRail: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val viewport = LocalAppViewport.current

    if (viewport.useNavigationRail) {
        Row(modifier = Modifier.fillMaxSize()) {
            navigationRail()
            Scaffold(
                modifier = Modifier.weight(1f),
                bottomBar = {},
                content = { innerPadding ->
                    content(innerPadding)
                }
            )
        }
    } else {
        Scaffold(
            bottomBar = bottomBar,
            content = { innerPadding ->
                content(innerPadding)
            }
        )
    }
}
