package pt.avante.app

/**
 * Endereços da própria app, que não são do jornal. Ficam fora de `data/avante/`
 * para a identidade da app se manter separável da da publicação (ADR 0002).
 */
object ProjectSite {
    /** Repositório público do código da app, mostrado no ecrã Sobre. */
    const val REPOSITORY_URL = "https://github.com/unclehaunted/civicnotebook"
}
