package pt.avante.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.ArticleEntity
import pt.avante.app.data.db.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PesquisaViewModel(
    private val db: AppDatabase,
    private val currentIssueStore: pt.avante.app.data.CurrentIssueStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val lastKnownCurrentIssueNumber: String?
        get() = currentIssueStore.lastKnownCurrentIssueNumber

    private val _searchResults = MutableStateFlow<List<NoteEntity>>(emptyList())
    val searchResults: StateFlow<List<NoteEntity>> = _searchResults.asStateFlow()

    val bookmarkedArticles: StateFlow<Set<String>> = db.articleDao().getBookmarkedArticleIds()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.Lazily,
            initialValue = emptySet()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchDatabase(query)
    }

    private fun searchDatabase(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                _searchResults.value = db.articleDao().searchNotes(query)
            }
        }
    }
}
