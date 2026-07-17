package pt.avante.app.ui.presentation

object ArticleTaxonomyFormatter {
    fun formatPillText(categoryLabel: String?, subcategoryLabel: String?): String? {
        val cat = categoryLabel?.trim()?.takeIf { it.isNotBlank() }
        val sub = subcategoryLabel?.trim()?.takeIf { it.isNotBlank() }
        
        return when {
            cat != null && sub != null -> "$cat · $sub"
            cat != null -> cat
            sub != null -> sub // Fallback, though rare
            else -> null
        }
    }
}
