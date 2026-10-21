package pt.avante.app.theme

import pt.avante.app.data.PapelDeCor
import pt.avante.app.data.TiposDeLetra

/**
 * O papel de um bloco de texto do artigo (#13): decide a face, o tamanho e a cor. O renderer
 * escolhe o papel pelo bloco; tudo o que distingue os papéis vive aqui.
 */
enum class PapelDoTexto(
    /** A Citação tem cor própria, um vermelho da família do acento (#7). */
    val cor: PapelDeCor
) {
    CORPO(PapelDeCor.TEXTO_PRIMARIO),
    PERGUNTA(PapelDeCor.TEXTO_PRIMARIO),
    CITACAO(PapelDeCor.CITACAO);

    /**
     * O ficheiro de fonte do papel nos [tipos] escolhidos. Itálico e negrito vêm de ficheiros
     * desenhados (#6), nunca de um estilo sintetizado. Citações na face de display; corpo e
     * Perguntas na face de texto (#13).
     */
    fun faceResId(tipos: TiposDeLetra): Int = when (this) {
        CORPO -> tipos.texto.regularFontResId
        PERGUNTA -> tipos.texto.boldFontResId
        CITACAO -> tipos.display.italicFontResId
    }

    /** O tamanho do papel na [escala] do Tamanho, em sp. */
    fun tamanhoSp(escala: EscalaDoArtigo): Float = when (this) {
        CORPO -> escala.corpo
        PERGUNTA -> escala.pergunta
        CITACAO -> escala.citacao
    }
}
