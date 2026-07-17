package pt.avante.app.ui.presentation

import pt.avante.app.data.ArticleSummary

object ArticlePresentationResolver {

    fun resolvePresentation(
        article: ArticleSummary,
        viewMode: FeedViewMode,
        repeatedImages: Set<String> = emptySet()
    ): ArticleCardPresentation {
        val category = article.categoryLabel?.trim()?.uppercase()
        val normalizedUrl = ArticleImageFrequencyAnalyzer.normalizeUrl(article.heroImageUrl)
        val hasRemoteImage = normalizedUrl != null && !repeatedImages.contains(normalizedUrl)

        return when (viewMode) {
            FeedViewMode.LIST -> resolveListMode(category, hasRemoteImage)
            FeedViewMode.GRID -> resolveGridMode(category, hasRemoteImage)
            FeedViewMode.STACK -> resolveStackMode()
            FeedViewMode.RELATED_GRID -> resolveRelatedGridMode()
        }
    }

    private fun resolveListMode(
        category: String?,
        hasRemoteImage: Boolean
    ): ArticleCardPresentation {
            return when (category) {
            "EM FOCO", "EDITORIAL" -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.LEAD_LARGE,
                spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                imagePolicy = if (hasRemoteImage) ArticleImagePolicy.REMOTE_IMAGE else ArticleImagePolicy.NO_IMAGE,
                showSummary = true,
                showFooter = true,
                showFavourite = true
            )
            "EM DESTAQUE", "INTERNACIONAL", "ARGUMENTOS" -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.FEATURED_MEDIUM,
                spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                imagePolicy = if (hasRemoteImage) ArticleImagePolicy.REMOTE_IMAGE else ArticleImagePolicy.NO_IMAGE,
                showSummary = true,
                showFooter = true,
                showFavourite = true
            )
            "OPINIÃO" -> ArticleCardPresentation(
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
                        variant = ArticlePresentationVariant.GRID_COMPACT_ROW, // Can be mapped to a compact text-only row in list too if desired, or STANDARD_SMALL_ROW with NO_IMAGE. Let's map to GRID_COMPACT_ROW or a compact text card.
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
        category: String?,
        hasRemoteImage: Boolean
    ): ArticleCardPresentation {
            return when (category) {
            "EM FOCO", "EDITORIAL" -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.LEAD_LARGE,
                spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                imagePolicy = if (hasRemoteImage) ArticleImagePolicy.REMOTE_IMAGE else ArticleImagePolicy.NO_IMAGE,
                showSummary = true,
                showFooter = true,
                showFavourite = true
            )
            "EM DESTAQUE", "INTERNACIONAL", "ARGUMENTOS" -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.FEATURED_MEDIUM,
                spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                imagePolicy = if (hasRemoteImage) ArticleImagePolicy.REMOTE_IMAGE else ArticleImagePolicy.NO_IMAGE,
                showSummary = true, // Medium cards in grid can show summary, or false if preferred.
                showFooter = true,
                showFavourite = true
            )
            "OPINIÃO" -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.OPINION_MEDIUM,
                spanPolicy = ArticleGridSpanPolicy.FULL_WIDTH,
                imagePolicy = ArticleImagePolicy.OPINION_AH_PLACEHOLDER,
                showSummary = false,
                showFooter = true,
                showFavourite = true
            )
            else -> ArticleCardPresentation(
                variant = ArticlePresentationVariant.GRID_TEXT_TILE,
                spanPolicy = ArticleGridSpanPolicy.SINGLE_CELL,
                imagePolicy = ArticleImagePolicy.NO_IMAGE,
                showSummary = false,
                showFooter = false,
                showFavourite = false
            )
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
