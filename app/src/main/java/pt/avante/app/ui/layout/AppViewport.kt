package pt.avante.app.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppViewport(
    val width: Dp,
    val height: Dp
) {
    val isCompactWidth: Boolean get() = width < 600.dp
    val isMediumWidth: Boolean get() = width >= 600.dp && width < 840.dp
    val isExpandedWidth: Boolean get() = width >= 840.dp
    
    val isCompactHeight: Boolean get() = height < 480.dp
    val isLandscape: Boolean get() = width > height
    
    val useNavigationRail: Boolean get() = !isCompactWidth
    
    val useWideReader: Boolean get() = width >= 600.dp && height >= 480.dp
}

val LocalAppViewport = staticCompositionLocalOf<AppViewport> {
    error("No AppViewport provided")
}

@Composable
fun ProvideAppViewport(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val viewport = AppViewport(
        width = configuration.screenWidthDp.dp,
        height = configuration.screenHeightDp.dp
    )
    CompositionLocalProvider(LocalAppViewport provides viewport) {
        content()
    }
}
