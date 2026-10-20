package pt.avante.app.viewmodel

import pt.avante.app.data.db.NoteEntity
import pt.avante.app.data.db.NoteType

/** A Nota livre que o botão + do Caderno cria, e quando se deita fora por ter ficado vazia. */
object NewNote {
    const val DEFAULT_TITLE = "Nova Nota"

    fun create(noteId: String, now: Long) = NoteEntity(
        noteId = noteId,
        title = DEFAULT_TITLE,
        bodyMarkdown = "",
        createdAt = now,
        updatedAt = now,
        noteType = NoteType.STANDALONE
    )

    /**
     * Uma Nova Nota que sai do editor sem corpo e com o título por omissão ou vazio
     * descarta-se. Só se aplica à Nota criada pelo + nesta sessão do editor: uma Nota
     * existente que o Leitor esvazie fica.
     */
    fun isLeftEmpty(note: NoteEntity): Boolean =
        note.bodyMarkdown.isBlank() && (note.title.isBlank() || note.title == DEFAULT_TITLE)
}
