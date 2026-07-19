package pt.avante.app.ui.components

import androidx.compose.ui.text.withStyle

object ProvenanceFormatter {
    fun formatProvenance(
        publication: String,
        issueNumber: String?,
        author: String?,
        currentIssueNumber: String?,
        subcategory: String? = null,
        hasNotes: Boolean = false,
        hasHighlights: Boolean = false
    ): androidx.compose.ui.text.AnnotatedString {
        return androidx.compose.ui.text.buildAnnotatedString {
            val cleanIssue = issueNumber?.filter { it.isDigit() }?.takeIf { it.isNotBlank() }
            val formattedAuthor = author?.let { pt.avante.app.ui.presentation.ArticleAuthorFormatter.formatAuthor(it) }?.takeIf { it.isNotBlank() }
            
            val cleanCurrentIssue = currentIssueNumber?.filter { it.isDigit() }?.takeIf { it.isNotBlank() }
            val isExplicitlyCurrent = cleanIssue != null && cleanIssue == cleanCurrentIssue

            val parts = buildList {
                add(publication)
                cleanIssue?.let(::add)
                // Author is handled specifically because we want it bold, so we will append parts manually
            }
            
            append(parts[0])
            if (parts.size > 1) {
                append(" · ")
                append(parts[1])
            }
            
            if (formattedAuthor != null) {
                append(" · ")
                withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                    append(formattedAuthor)
                }
            }
            
            if (isExplicitlyCurrent) {
                append(" · Edição Atual")
            }
        }
    }
}
