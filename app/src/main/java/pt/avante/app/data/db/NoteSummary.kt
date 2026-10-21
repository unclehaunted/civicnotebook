package pt.avante.app.data.db

/**
 * O que o cartão de uma Nota mostra do corpo em markdown: a primeira citação
 * (`>`) como Excerto, a primeira linha de texto e as etiquetas (`#palavra`).
 */
data class NoteSummary(
    /** A primeira linha começada por `>`, sem o marcador e sem espaços à volta. */
    val excerpt: String?,
    /** A primeira linha não vazia que não é citação (`>`) nem começa por `#`. */
    val firstLine: String?,
    /** As etiquetas `#palavra` do corpo, sem repetições, pela ordem em que aparecem. */
    val tags: List<String>
) {
    companion object {
        private val TAG = Regex("""#[\p{L}\p{N}_-]+""")

        fun of(bodyMarkdown: String): NoteSummary {
            val lines = bodyMarkdown.split("\n")
            return NoteSummary(
                excerpt = lines.firstOrNull { it.startsWith(">") }?.removePrefix(">")?.trim(),
                firstLine = lines.firstOrNull { !it.startsWith(">") && !it.startsWith("#") && it.isNotBlank() },
                tags = TAG.findAll(bodyMarkdown).map { it.value }.distinct().toList()
            )
        }
    }
}

/** O resumo do corpo desta Nota (ver [NoteSummary]). */
fun NoteEntity.summary(): NoteSummary = NoteSummary.of(bodyMarkdown)
