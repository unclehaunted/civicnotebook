package pt.avante.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.deleteNoteWithSources

sealed class NoteEditorUiState {
    object Loading : NoteEditorUiState()
    data class Success(val note: NoteEntity, val articleSourceId: String? = null) : NoteEditorUiState()
    data class Error(val message: String) : NoteEditorUiState()
}

class NoteViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow<NoteEditorUiState>(NoteEditorUiState.Loading)
    val uiState: StateFlow<NoteEditorUiState> = _uiState.asStateFlow()

    private var currentNote: NoteEntity? = null

    // A Nova Nota que o + criou e abriu neste editor: só essa se descarta ao sair, se ficar vazia.
    private var newNoteId: String? = null

    private var currentSourceId: String? = null
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    private var saveJob: Job? = null

    private var loadJob: Job? = null

    fun loadNote(noteId: String, isNewNote: Boolean = false) {
        newNoteId = noteId.takeIf { isNewNote }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = NoteEditorUiState.Loading
            try {
                val note = db.articleDao().getNote(noteId)
                if (note != null) {
                    currentNote = note
                    val source = db.articleDao().getNoteSource(noteId)
                    currentSourceId = source?.articleId
                    _uiState.value = NoteEditorUiState.Success(note, currentSourceId)
                } else {
                    _uiState.value = NoteEditorUiState.Error("Nota não encontrada.")
                }
            } catch (e: Exception) {
                _uiState.value = NoteEditorUiState.Error("Erro: ${e.message}")
            }
        }
    }

    fun updateTitle(title: String) {
        val note = currentNote ?: return
        val updated = note.copy(title = title, updatedAt = System.currentTimeMillis())
        currentNote = updated
        _uiState.value = NoteEditorUiState.Success(updated, currentSourceId)
        debouncedSave()
    }

    fun updateBody(body: String) {
        val note = currentNote ?: return
        val updated = note.copy(bodyMarkdown = body, updatedAt = System.currentTimeMillis())
        currentNote = updated
        _uiState.value = NoteEditorUiState.Success(updated, currentSourceId)
        debouncedSave()
    }

    /**
     * Sair do editor: descarta uma Nova Nota que ficou vazia ou grava já o que ainda
     * esperava pelo debounce. O Job deixa quem chama esperar pelo fim.
     */
    fun closeNote(): Job {
        // Uma Nova Nota que ainda não acabou de carregar está, por definição, vazia.
        loadJob?.cancel()
        val closing = currentNote?.takeIf { it.noteId == newNoteId || newNoteId == null }
        val discardId = newNoteId?.takeIf { closing == null || NewNote.isLeftEmpty(closing) }
        val hasPendingSave = saveJob?.isActive == true
        saveJob?.cancel()
        _isSaving.value = false
        newNoteId = null
        currentNote = null
        return viewModelScope.launch {
            when {
                discardId != null -> db.deleteNoteWithSources(discardId)
                closing != null && hasPendingSave -> db.articleDao().insertNote(closing)
            }
        }
    }

    private fun debouncedSave() {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(800) // Wait 800ms before saving
            saveNote()
        }
    }

    private suspend fun saveNote() {
        val note = currentNote ?: return
        _isSaving.value = true
        db.articleDao().insertNote(note)
        delay(300) // Show "A guardar..." for at least 300ms
        _isSaving.value = false
    }
}
