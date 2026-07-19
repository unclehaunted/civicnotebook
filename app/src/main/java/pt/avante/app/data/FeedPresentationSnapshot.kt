package pt.avante.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class FeedPresentationSnapshot(
    val issueNumber: String?,
    val editionLabel: String,
    val articles: List<ArticlePresentationSnapshot>
)

@Serializable
data class ArticlePresentationSnapshot(
    val canonicalUrl: String,
    val isPremium: Boolean,
    val isFree: Boolean,
    val visualPlacement: String?,
    val cardTreatment: String // Storing as String enum equivalent
)
