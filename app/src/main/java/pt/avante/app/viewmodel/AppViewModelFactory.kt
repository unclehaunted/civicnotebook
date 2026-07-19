package pt.avante.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.avante.app.data.db.AppDatabase
import pt.avante.app.data.CurrentIssueStore

class AppViewModelFactory(
    private val db: AppDatabase,
    private val currentIssueStore: CurrentIssueStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(db, currentIssueStore) as T
            }
            modelClass.isAssignableFrom(PesquisaViewModel::class.java) -> {
                PesquisaViewModel(db, currentIssueStore) as T
            }
            modelClass.isAssignableFrom(CadernoViewModel::class.java) -> {
                CadernoViewModel(db) as T
            }
            modelClass.isAssignableFrom(ReaderViewModel::class.java) -> {
                ReaderViewModel(db) as T
            }
            modelClass.isAssignableFrom(NoteViewModel::class.java) -> {
                NoteViewModel(db) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
