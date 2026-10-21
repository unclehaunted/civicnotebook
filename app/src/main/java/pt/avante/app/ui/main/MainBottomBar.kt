package pt.avante.app.ui.main

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.TextSecondary

/** Barra inferior em ecrã estreito: os quatro separadores sempre visíveis, um toque muda de ecrã. */
@Composable
fun MainBottomBar(
    currentTab: MainDestination,
    onTabSelected: (MainDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(containerColor = Color.White, modifier = modifier) {
        MainDestination.entries.forEach { dest ->
            NavigationBarItem(
                icon = { Icon(dest.icon, contentDescription = null) },
                label = { Text(dest.label) },
                selected = currentTab == dest,
                onClick = { onTabSelected(dest) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OriginalRed,
                    selectedTextColor = OriginalRed,
                    indicatorColor = OriginalRed.copy(alpha = 0.1f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}
