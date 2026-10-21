package pt.avante.app.ui.main

import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.TextSecondary

/**
 * As cores dos separadores principais, as mesmas na barra inferior (ecrã
 * estreito) e no trilho lateral (ecrã largo).
 */
internal object MainNavColors {
    val container = Color.White
    private val selected = OriginalRed
    private val indicator = OriginalRed.copy(alpha = 0.1f)
    private val unselected = TextSecondary

    @Composable
    fun barItem(): NavigationBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = selected,
        selectedTextColor = selected,
        indicatorColor = indicator,
        unselectedIconColor = unselected,
        unselectedTextColor = unselected
    )

    @Composable
    fun railItem(): NavigationRailItemColors = NavigationRailItemDefaults.colors(
        selectedIconColor = selected,
        selectedTextColor = selected,
        indicatorColor = indicator,
        unselectedIconColor = unselected,
        unselectedTextColor = unselected
    )
}
