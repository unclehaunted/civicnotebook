package pt.avante.app.ui.main

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Barra inferior em ecrã estreito: os quatro separadores sempre visíveis, um toque muda de ecrã. */
@Composable
fun MainBottomBar(
    currentTab: MainDestination,
    onTabSelected: (MainDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(containerColor = MainNavColors.container, modifier = modifier) {
        MainDestination.entries.forEach { dest ->
            NavigationBarItem(
                icon = { Icon(dest.icon, contentDescription = null) },
                label = { Text(dest.label) },
                selected = currentTab == dest,
                onClick = { onTabSelected(dest) },
                colors = MainNavColors.barItem()
            )
        }
    }
}
