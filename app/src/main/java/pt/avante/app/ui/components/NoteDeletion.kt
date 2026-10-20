package pt.avante.app.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch
import pt.avante.app.data.db.DeletedNote

/**
 * "Eliminar" de uma Nota, igual no Caderno e na Pesquisa: apaga já e mostra
 * "Nota eliminada" com "Anular". Se o Snackbar expira, ou o ecrã sai da composição
 * e cancela o scope, a eliminação fica definitiva.
 */
@Composable
fun rememberDeleteNoteWithUndo(
    snackbarHostState: SnackbarHostState,
    removeNote: suspend (String) -> DeletedNote?,
    restoreNote: (DeletedNote) -> Unit
): (String) -> Unit {
    val scope = rememberCoroutineScope()
    val currentRemoveNote by rememberUpdatedState(removeNote)
    val currentRestoreNote by rememberUpdatedState(restoreNote)
    return remember(snackbarHostState, scope) {
        { noteId ->
            scope.launch {
                val deleted = currentRemoveNote(noteId) ?: return@launch
                // Uma eliminação nova torna definitiva a anterior, em vez de esperar na fila.
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = "Nota eliminada",
                    actionLabel = "Anular",
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) currentRestoreNote(deleted)
            }
        }
    }
}
