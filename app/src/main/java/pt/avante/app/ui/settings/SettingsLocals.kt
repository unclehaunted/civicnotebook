package pt.avante.app.ui.settings

import androidx.compose.runtime.compositionLocalOf
import pt.avante.app.data.ReaderSettings

/** Preferências de leitura atuais, providas no topo da árvore (MainActivity). */
val LocalReaderSettings = compositionLocalOf { ReaderSettings() }

/** Ação para abrir o ecrã de Definições (provida pelo MainApp). Null fora do contexto principal. */
val LocalOpenSettings = compositionLocalOf<(() -> Unit)?> { null }
