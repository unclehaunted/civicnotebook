package pt.avante.app.theme

import pt.avante.app.data.ReaderFontSize

/*
 * Escalas modulares (#11 da spec #5): tamanhos em sp, como Float, sem Compose. A UI só os aplica.
 * A entrelinha e o espaço entre parágrafos não vivem aqui: são do Espaçamento (#12).
 */

/** Razão das duas escalas: cada degrau é o anterior × 1,25 (confirmada no protótipo, #7). */
const val RAZAO_DA_ESCALA = 1.25f

/** Corpo do chrome: o degrau de onde a escala fixa do chrome parte. */
private const val BASE_DO_CHROME = 16f

/**
 * Escala fixa do chrome (cartões, listas, separadores, barras, masthead): 16 × 1,25ⁿ.
 * Não segue o Tamanho, só o tamanho de letra do sistema (sp). Nenhum degrau abaixo de 12sp.
 */
enum class DegrauDoChrome(val sp: Float) {
    PEQUENO(BASE_DO_CHROME / RAZAO_DA_ESCALA),
    BASE(BASE_DO_CHROME),
    MEDIO(BASE_DO_CHROME * RAZAO_DA_ESCALA),
    GRANDE(BASE_DO_CHROME * RAZAO_DA_ESCALA * RAZAO_DA_ESCALA)
}

/**
 * Tamanhos do artigo e do editor de Notas, a partir do corpo do Tamanho.
 * Pergunta e Citação ficam no corpo: distinguem-se pelo negrito e pelo itálico, não pelo tamanho.
 */
data class EscalaDoArtigo(
    val corpo: Float,
    val pergunta: Float,
    val citacao: Float,
    val subtitulo: Float,
    val titulo: Float,
    /** Um degrau abaixo do título de um artigo. */
    val tituloDaNota: Float
)

fun escalaDoArtigo(tamanho: ReaderFontSize): EscalaDoArtigo {
    val corpo = tamanho.spValue
    val subtitulo = corpo * RAZAO_DA_ESCALA
    return EscalaDoArtigo(
        corpo = corpo,
        pergunta = corpo,
        citacao = corpo,
        subtitulo = subtitulo,
        titulo = subtitulo * RAZAO_DA_ESCALA,
        tituloDaNota = subtitulo
    )
}

/** Tracking dos rótulos em maiúsculas, em em; o texto em minúsculas não leva tracking. */
const val TRACKING_DE_MAIUSCULAS_EM = 0.08f
