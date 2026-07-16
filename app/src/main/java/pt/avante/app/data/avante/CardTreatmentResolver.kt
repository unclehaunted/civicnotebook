package pt.avante.app.data.avante

import pt.avante.app.data.CardTreatment

object CardTreatmentResolver {
    fun resolveCardTreatment(
        categoryLabel: String?,
        subcategoryLabel: String?,
        visualPlacement: String?,
        homepageOrder: Int,
        hasUsefulImage: Boolean
    ): CardTreatment {
        val visual = visualPlacement?.trim()

        val isOpinionPlacement = visual.equals("Opinião", ignoreCase = true) ||
            visual.equals("Opiniao", ignoreCase = true) ||
            visual.equals("Actual", ignoreCase = true)

        val isOpinionFallback = visual.isNullOrBlank() &&
            categoryLabel.equals("Opinião", ignoreCase = true)

        return when {
            visual.equals("Em Foco", ignoreCase = true) || homepageOrder == 0 ->
                CardTreatment.LEAD_LARGE

            visual.equals("Em Destaque", ignoreCase = true) || visual.equals("Destaque", ignoreCase = true) ->
                CardTreatment.FEATURED_MEDIUM

            isOpinionPlacement || isOpinionFallback ->
                CardTreatment.OPINION_COMPACT

            !hasUsefulImage ->
                CardTreatment.TEXT_ONLY

            else ->
                CardTreatment.STANDARD_SMALL
        }
    }
}
