package pt.avante.app.data.avante

/** Endereços e marcas do avante.pt de que a app depende, num só sítio. */
object AvanteSite {
    const val BASE_URL = "https://avante.pt"
    const val AUTH_URL = "$BASE_URL/auth"

    /** Página da editora onde se compra ou assina o jornal. */
    const val SUBSCRIPTION_URL = "https://editorial-avante.pcp.pt/avante.html"

    /** Texto que o avante.pt põe no lugar do corpo quando o artigo é pago e não há sessão. */
    const val PAYWALL_MARKER = "O artigo completo está disponível"

    /** Índice da edição (tpl=183), o inventário canónico dos artigos. */
    fun indexUrl(editionNumber: String) = "$BASE_URL/pt/$editionNumber/?tpl=183"
}

/** Endereços da própria app, que não são do avante.pt. */
object ProjectSite {
    /** Repositório público do código da app, mostrado no ecrã Sobre. */
    const val REPOSITORY_URL = "https://github.com/unclehaunted/civicnotebook"
}
