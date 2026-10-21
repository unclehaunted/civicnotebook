package pt.avante.app.ui.main

import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import pt.avante.app.theme.Cores

/**
 * As cores dos separadores principais, as mesmas na barra inferior (ecrã
 * estreito) e no trilho lateral (ecrã largo), na Aparência escolhida.
 */
internal object MainNavColors {
    val container: Color @Composable get() = Cores.cartao
    private val selected: Color @Composable get() = Cores.acento
    private val indicator: Color @Composable get() = Cores.acento.copy(alpha = 0.1f)
    private val unselected: Color @Composable get() = Cores.textoSecundario

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
