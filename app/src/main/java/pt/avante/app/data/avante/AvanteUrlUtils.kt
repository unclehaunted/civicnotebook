package pt.avante.app.data.avante

object AvanteUrlUtils {
    /**
     * Normaliza uma URL relativa ou absoluta para URL absoluta.
     * 
     * @param url URL a normalizar (pode ser relativa ou absoluta)
     * @param baseUrl URL base a usar para URLs relativas (default: [AvanteSite.BASE_URL])
     * @return URL absoluta normalizada
     */
    fun normalizeUrl(url: String, baseUrl: String = AvanteSite.BASE_URL): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("/") -> "$baseUrl$url"
            else -> "$baseUrl/$url"
        }
    }
}
