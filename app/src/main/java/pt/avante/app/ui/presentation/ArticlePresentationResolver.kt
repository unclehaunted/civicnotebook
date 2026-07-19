package pt.avante.app.ui.presentation

import pt.avante.app.data.ArticleSummary

object ArticlePresentationResolver {

    fun resolvePresentation(
        article: ArticleSummary,
        viewMode: FeedViewMode
    ): ArticleCardPresentation {
        val category = article.categoryLabel?.trim()?.uppercase()
        val hasRemoteImage = article.heroImageUrl != null

        return when (viewMode) {
            FeedViewMode.LIST -> resolveListMode(article, hasRemoteImage)
            FeedViewMode.GRID -> resolveGridMode(article, hasRemoteImage)
            FeedViewMode.STACK -> resolveStackMode()
            FeedViewMode.RELATED_GRID -> resolveRelatedGridMode()
        }
    }

    private fun resolveListMode(
        article: ArticleSummary,
        hasRemoteImage: Boolean
    ): ArticleCardPresentation {
        return when (article.cardTreatment) {
            pt.avante.app.data.CardTreatment.LEAD_LARGE -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.LEAD_LARGE,
                spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                imagePolicy = if (hasRemoteImage) ArticleImagePolicy.REMOTE_IMAGE else ArticleImagePolicy.NO_IMAGE,
                showSummary = true,
                showFooter = true,
                showFavourite = true
            )
            pt.avante.app.data.CardTreatment.FEATURED_MEDIUM -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.FEATURED_MEDIUM,
                spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                imagePolicy = if (hasRemoteImage) ArticleImagePolicy.REMOTE_IMAGE else ArticleImagePolicy.NO_IMAGE,
                showSummary = true,
                showFooter = true,
                showFavourite = true
            )
            pt.avante.app.data.CardTreatment.OPINION_COMPACT -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.OPINION_MEDIUM,
                spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                imagePolicy = ArticleImagePolicy.OPINION_AH_PLACEHOLDER,
                showSummary = false,
                showFooter = true,
                showFavourite = true
            )
            else -> {
                if (hasRemoteImage) {
                    ArticleCardPresentation(
                        variant = ArticlePresentationVariant.STANDARD_SMALL_ROW,
                        spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                        imagePolicy = ArticleImagePolicy.REMOTE_IMAGE,
                        showSummary = false,
                        showFooter = false,
                        showFavourite = false
                    )
                } else {
                    ArticleCardPresentation(
                        variant = ArticlePresentationVariant.GRID_COMPACT_ROW,
                        spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                        imagePolicy = ArticleImagePolicy.NO_IMAGE,
                        showSummary = false,
                        showFooter = false,
                        showFavourite = false
                    )
                }
            }
        }
    }

    private fun resolveGridMode(
        article: ArticleSummary,
        hasRemoteImage: Boolean
    ): ArticleCardPresentation {
        return when (article.cardTreatment) {
            pt.avante.app.data.CardTreatment.LEAD_LARGE -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.LEAD_LARGE,
                spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                imagePolicy = if (hasRemoteImage) ArticleImagePolicy.REMOTE_IMAGE else ArticleImagePolicy.NO_IMAGE,
                showSummary = true,
                showFooter = true,
                showFavourite = true
            )
            pt.avante.app.data.CardTreatment.FEATURED_MEDIUM -> ArticleCardPresentation(
                variant = if (hasRemoteImage) ArticlePresentationVariant.GRID_IMAGE_TILE else ArticlePresentationVariant.GRID_TEXT_TILE,
                spanPolicy = ArticleGridSpanPolicy.SINGLE_CELL,
                imagePolicy = if (hasRemoteImage) ArticleImagePolicy.REMOTE_IMAGE else ArticleImagePolicy.NO_IMAGE,
                showSummary = false,
                showFooter = true,
                showFavourite = true
            )
            else -> {
                if (hasRemoteImage) {
                    ArticleCardPresentation(
                        variant = ArticlePresentationVariant.GRID_IMAGE_TILE,
                        spanPolicy = ArticleGridSpanPolicy.SINGLE_CELL,
                        imagePolicy = ArticleImagePolicy.REMOTE_IMAGE,
                        showSummary = false,
                        showFooter = true,
                        showFavourite = true
                    )
                } else {
                    ArticleCardPresentation(
                        variant = ArticlePresentationVariant.GRID_TEXT_TILE,
                        spanPolicy = ArticleGridSpanPolicy.SINGLE_CELL,
                        imagePolicy = ArticleImagePolicy.NO_IMAGE,
                        showSummary = false,
                        showFooter = true,
                        showFavourite = true
                    )
                }
            }
        }
    }

    private fun resolveStackMode(): ArticleCardPresentation {
        return ArticleCardPresentation(
            variant = ArticlePresentationVariant.STACK_PAGE,
            spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
            imagePolicy = ArticleImagePolicy.REMOTE_IMAGE,
            showSummary = true,
            showFooter = true,
            showFavourite = true
        )
    }

    private fun resolveRelatedGridMode(): ArticleCardPresentation {
        return ArticleCardPresentation(
            variant = ArticlePresentationVariant.RELATED_TILE,
            spanPolicy = ArticleGridSpanPolicy.SINGLE_CELL,
            imagePolicy = ArticleImagePolicy.REMOTE_IMAGE, // Can fallback to NO_IMAGE if null, handled by component
            showSummary = false,
            showFooter = false,
            showFavourite = false
        )
    }
}
