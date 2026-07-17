package pt.avante.app.ui.presentation

data class ArticleCardPresentation(
    val variant: ArticlePresentationVariant,
    val spanPolicy: ArticleGridSpanPolicy,
    val imagePolicy: ArticleImagePolicy,
    val showSummary: Boolean,
    val showFooter: Boolean,
    val showFavourite: Boolean
)
