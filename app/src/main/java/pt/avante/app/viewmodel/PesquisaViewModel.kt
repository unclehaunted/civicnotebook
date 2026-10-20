package pt.avante.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pt.avante.app.data.ArticleSummary
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.db.DeletedNote
import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.deleteNoteWithSources
import pt.avante.app.data.db.restoreNote
import pt.avante.app.data.toSearchKey
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MS = 5_000L

class PesquisaViewModel(
    private val db: AppDatabase,
    private val currentIssueStore: pt.avante.app.data.CurrentIssueStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val lastKnownCurrentIssueNumber: String?
        get() = currentIssueStore.lastKnownCurrentIssueNumber

    // Os resultados combinam sempre o texto atual com a lista atual, por isso nunca mostram
    // os de um texto anterior. A chave de cada Nota e artigo calcula-se quando a lista muda,
    // e cada tecla só compara texto já normalizado.
    private val searchableNotes = db.articleDao().observeAllNotes()
        .map { notes -> notes.map { Searchable(it, it.searchKey()) } }

    private val currentEditionArticles = MutableStateFlow<List<Searchable<ArticleSummary>>>(emptyList())

    val noteResults: StateFlow<List<NoteEntity>> =
        combine(_searchQuery, searchableNotes) { query, notes -> notes.matching(query) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val editionResults: StateFlow<List<ArticleSummary>> =
        combine(_searchQuery, currentEditionArticles) { query, articles -> articles.matching(query) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val bookmarkedArticles: StateFlow<Set<String>> = db.articleDao().getBookmarkedArticleIds()
        .map { it.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptySet()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /** Os artigos da Edição corrente onde a Pesquisa procura. */
    fun onCurrentEditionLoaded(articles: List<ArticleSummary>) {
        currentEditionArticles.value = articles.map { Searchable(it, it.searchKey()) }
    }

    /** Apaga a Nota; os resultados atualizam-se sozinhos. Devolve o que é preciso para anular. */
    suspend fun removeNote(id: String): DeletedNote? = db.deleteNoteWithSources(id)

    /** Repõe uma Nota eliminada; os resultados atualizam-se sozinhos. */
    fun restoreNote(deleted: DeletedNote) {
        viewModelScope.launch { db.restoreNote(deleted) }
    }
}

/** Um item da Pesquisa com a sua chave de comparação já calculada. */
private class Searchable<T>(val item: T, val key: String)

private fun <T> List<Searchable<T>>.matching(query: String): List<T> {
    if (query.isBlank()) return emptyList()
    val queryKey = query.toSearchKey()
    return filter { it.key.contains(queryKey) }.map { it.item }
}

// Os campos juntam-se com quebra de linha, que o texto de pesquisa (uma linha) nunca contém:
// assim um termo não encontra o fim de um campo colado ao início do seguinte.
private fun NoteEntity.searchKey(): String = "$title\n$bodyMarkdown".toSearchKey()

private fun ArticleSummary.searchKey(): String =
    listOfNotNull(title, author, categoryLabel, subcategoryLabel, summary).joinToString("\n").toSearchKey()
