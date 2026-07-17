package pt.avante.app.ui.presentation

object ArticleAuthorFormatter {
    fun formatAuthor(author: String?): String {
        val raw = author?.trim()
        if (raw.isNullOrBlank()) return "Redacção"
        
        // Remove parenthetical contexts like "(Membro da Comissão Política)"
        return raw.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
    }
}
