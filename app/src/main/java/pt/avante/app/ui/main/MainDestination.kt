package pt.avante.app.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/** Os separadores da navegação principal, pela ordem em que aparecem na barra e no rail. */
enum class MainDestination(val label: String, val icon: ImageVector) {
    EDICAO("Edição", Icons.AutoMirrored.Filled.MenuBook),
    CADERNO("Caderno", Icons.AutoMirrored.Filled.Note),
    ARQUIVO("Arquivo", Icons.Default.Folder),
    PESQUISA("Pesquisa", Icons.Default.Search)
}
