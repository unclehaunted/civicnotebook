package pt.avante.app.ui.presentation

import pt.avante.app.data.DEFAULT_ARTICLE_AUTHOR

object ArticleAuthorFormatter {
    fun formatAuthor(author: String?): String {
        val raw = author?.trim()
        if (raw.isNullOrBlank()) return DEFAULT_ARTICLE_AUTHOR
        
        // Remove parenthetical contexts like "(Membro da Comissão Política)"
        return raw.replace(Regex("\\s*\\([^)]*\\)"), "").trim()
    }
}
