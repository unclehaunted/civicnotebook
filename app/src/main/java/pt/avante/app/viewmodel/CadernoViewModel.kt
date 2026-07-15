package pt.avante.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CadernoViewModel(private val db: AppDatabase) : ViewModel() {

    private val _uiState = MutableStateFlow<CadernoUiState>(CadernoUiState.Loading)
    val uiState: StateFlow<CadernoUiState> = _uiState.asStateFlow()

    fun loadNotes() {
        viewModelScope.launch {
            _uiState.value = CadernoUiState.Loading
            val notes = db.articleDao().getAllNotes()
            if (notes.isEmpty()) {
                _uiState.value = CadernoUiState.Empty
            } else {
                _uiState.value = CadernoUiState.Success(notes)
            }
        }
    }
    
    fun removeNote(id: String) {
        viewModelScope.launch {
            db.articleDao().deleteNote(id)
            loadNotes()
        }
    }
}

sealed class CadernoUiState {
    object Loading : CadernoUiState()
    object Empty : CadernoUiState()
    data class Success(val notes: List<NoteEntity>) : CadernoUiState()
}
