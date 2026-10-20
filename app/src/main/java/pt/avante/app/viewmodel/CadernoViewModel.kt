package pt.avante.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.DeletedNote
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.deleteNoteWithSources
import pt.avante.app.data.db.restoreNote
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

private const val STOP_TIMEOUT_MS = 5_000L

class CadernoViewModel(private val db: AppDatabase) : ViewModel() {

    // Observa as Notas: eliminar, anular ou descartar uma Nova Nota no editor atualiza a lista sozinho.
    val uiState: StateFlow<CadernoUiState> = db.articleDao().observeAllNotes()
        .map { notes -> if (notes.isEmpty()) CadernoUiState.Empty else CadernoUiState.Success(notes) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CadernoUiState.Loading)

    /** Apaga a Nota; devolve o que é preciso para anular. */
    suspend fun removeNote(id: String): DeletedNote? = db.deleteNoteWithSources(id)

    /** Repõe uma Nota eliminada; a lista atualiza-se sozinha. */
    fun restoreNote(deleted: DeletedNote) {
        viewModelScope.launch { db.restoreNote(deleted) }
    }

    /** Grava uma Nova Nota e só depois devolve o id, para o editor nunca a procurar antes de existir. */
    suspend fun createNewNote(): String {
        val note = NewNote.create(noteId = UUID.randomUUID().toString(), now = System.currentTimeMillis())
        db.articleDao().insertNote(note)
        return note.noteId
    }
}

sealed class CadernoUiState {
    object Loading : CadernoUiState()
    object Empty : CadernoUiState()
    data class Success(val notes: List<NoteEntity>) : CadernoUiState()
}
