package pt.avante.app.data.avante

object AvanteCategoryRules {
    /**
     * Mapa de categorias válidas conhecidas do Avante!
     * Chave: versão normalizada (lowercase, sem acentos)
     * Valor: label canónico para display
     */
    private val VALID_CATEGORIES = mapOf(
        "editorial" to "Editorial",
        "opiniao" to "Opinião",
        "em destaque" to "Em Destaque",
        "emdestaque" to "Em Destaque",
        "em foco" to "Em Foco",
        "emfoco" to "Em Foco",
        "pcp" to "PCP",
        "trabalhadores" to "Trabalhadores",
        "assembleia da republica" to "Assembleia da República",
        "assembleiadarepublica" to "Assembleia da República",
        "nacional" to "Nacional",
        "internacional" to "Internacional",
        "temas" to "Temas",
        "argumentos" to "Argumentos",
        "memoria" to "Memória",
        "sugestoes da semana" to "Sugestões da semana",
        "sugestoes" to "Sugestões da semana",
        "aconteceu" to "Aconteceu",
        "agenda" to "Agenda",
        "resumo da semana" to "Resumo da Semana",
        "resumosemana" to "Resumo da Semana",
        "frases" to "Frases"
    )

    /**
     * Normaliza texto para comparação: lowercase, remove acentos, colapsa espaços.
     */
    fun normalizeForComparison(text: String): String {
        return text.trim()
            .lowercase()
            .replace("á", "a")
            .replace("à", "a")
            .replace("â", "a")
            .replace("ã", "a")
            .replace("é", "e")
            .replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("ô", "o")
            .replace("õ", "o")
            .replace("ú", "u")
            .replace("ç", "c")
            .replace(Regex("\\s+"), " ")
    }

    /**
     * Valida e retorna o label canónico de uma categoria.
     * Retorna null se o texto não for uma categoria válida.
     * 
     * @param rawText Texto candidato a categoria
     * @return Label canónico da categoria ou null
     */
    fun canonicalCategoryLabel(rawText: String?): String? {
        if (rawText == null) return null
        
        val trimmed = rawText.trim()
        
        if (trimmed.length > 50) return null
        
        if (trimmed.contains("?") || trimmed.contains("!") || trimmed.contains(":")) return null
        
        if (trimmed.contains("Índice", ignoreCase = true) || 
            trimmed.contains("Indice", ignoreCase = true)) return null
        
        val normalized = normalizeForComparison(trimmed)
        return VALID_CATEGORIES[normalized]
    }
}
