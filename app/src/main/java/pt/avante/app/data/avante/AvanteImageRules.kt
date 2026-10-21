package pt.avante.app.data.avante

object AvanteImageRules {
    /**
     * Normaliza uma URL de imagem, convertendo URLs relativas em absolutas.
     * Retorna null se a imagem for placeholder/logo.
     * 
     * @param imageUrl URL da imagem a normalizar (pode ser null)
     * @param baseUrl URL base a usar para URLs relativas (default: [AvanteSite.BASE_URL])
     * @return URL absoluta normalizada ou null se for placeholder
     */
    fun normalizeImageUrl(imageUrl: String?, baseUrl: String = AvanteSite.BASE_URL): String? {
        if (imageUrl == null) return null
        if (isPlaceholderImage(imageUrl)) return null
        
        return when {
            imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> imageUrl
            imageUrl.startsWith("/") -> "$baseUrl$imageUrl"
            else -> "$baseUrl/$imageUrl"
        }
    }

    /**
     * Verifica se uma URL de imagem é um placeholder/logo que deve ser ignorado.
     * 
     * @param imageUrl URL da imagem a verificar
     * @return true se for placeholder, false caso contrário (null retorna false)
     */
    fun isPlaceholderImage(imageUrl: String?): Boolean {
        if (imageUrl == null || imageUrl.isBlank()) return false
        
        val placeholderPatterns = listOf(
            "simbolo_foice",
            "ImageId=41",
            "pcp22px.png",
            "omilitante22px.png",
            "festa_avante",
            "avante_clandestino",
            "avante.gif",
            "/themes/publication_2/theme_2/img/",
            "ImageId=49919",
            "LOCAL_SYMBOL"
        )
        
        return placeholderPatterns.any { pattern -> imageUrl.contains(pattern) }
    }

    /** Uma fotografia do CMS (`/images/cms-image-` ou `get_img?…ImageId=`), não um ícone da página. */
    fun isArticleImage(imageUrl: String): Boolean =
        ("/images/cms-image-" in imageUrl || "ImageId=" in imageUrl) && !isPlaceholderImage(imageUrl)
}
