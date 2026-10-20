package pt.avante.app.data.db

import androidx.room.withTransaction

/** Uma Nota eliminada com as suas fontes, guardada enquanto a eliminação ainda se pode anular. */
data class DeletedNote(
    val note: NoteEntity,
    val sources: List<NoteSourceEntity>
)

// As transações vivem aqui e não no DAO: o Room 2.6 com KSP2 não gera `@Transaction open suspend fun`.

/** Apaga a Nota e as suas fontes; devolve o que apagou, para se poder anular com [restoreNote]. */
suspend fun AppDatabase.deleteNoteWithSources(id: String): DeletedNote? = withTransaction {
    val dao = articleDao()
    val note = dao.getNote(id) ?: return@withTransaction null
    val sources = dao.getNoteSources(id)
    dao.deleteNote(id)
    dao.deleteNoteSources(id)
    DeletedNote(note, sources)
}

/** Volta a gravar a Nota e as suas fontes exatamente como estavam antes de [deleteNoteWithSources]. */
suspend fun AppDatabase.restoreNote(deleted: DeletedNote) {
    withTransaction {
        articleDao().insertNote(deleted.note)
        articleDao().insertNoteSources(deleted.sources)
    }
}
