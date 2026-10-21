package pt.avante.app.data

/**
 * Resolver da Aparência (#8, ADR 0001): a única fonte de cor da app.
 *
 * Cada Aparência resolve todos os papéis de cor, do artigo ao chrome, em ARGB opaco e sem
 * Compose; a UI só os aplica (`theme/Theme.kt`). O vermelho é o único acento; o amarelo do
 * Destaque é a única outra cor com função.
 */
enum class PapelDeCor {
    /** Fundo de cada ecrã, incluindo o do artigo. */
    FUNDO,

    /** Folhas, barras, menus e faixas sobre o fundo. */
    SUPERFICIE,

    /** Cartões das listas (Edição, Caderno, Pesquisa). */
    CARTAO,

    /** Linhas de 1dp entre secções e contornos. */
    DIVISOR,
    TEXTO_PRIMARIO,

    /** Datas, legendas, rótulos e ícones discretos. */
    TEXTO_SECUNDARIO,

    /** O vermelho do jornal: marca, seleção, ações. */
    ACENTO,

    /** Texto e ícones pousados sobre o acento. */
    SOBRE_ACENTO,

    /** Fundo de uma passagem destacada. */
    DESTAQUE,

    /** Texto de uma Citação no artigo: vermelho escuro, da família do acento (#7). */
    CITACAO,

    /** As palavras do Leitor nas suas Notas. */
    ESCRITA
}

data class Paleta(
    val fundo: Long,
    val superficie: Long,
    val cartao: Long,
    val divisor: Long,
    val textoPrimario: Long,
    val textoSecundario: Long,
    val acento: Long,
    val sobreAcento: Long,
    val destaque: Long,
    val citacao: Long,
    val escrita: Long,
    /** Aparência escura: os ícones da barra de estado passam a claros. */
    val escura: Boolean = false
) {
    fun cor(papel: PapelDeCor): Long = when (papel) {
        PapelDeCor.FUNDO -> fundo
        PapelDeCor.SUPERFICIE -> superficie
        PapelDeCor.CARTAO -> cartao
        PapelDeCor.DIVISOR -> divisor
        PapelDeCor.TEXTO_PRIMARIO -> textoPrimario
        PapelDeCor.TEXTO_SECUNDARIO -> textoSecundario
        PapelDeCor.ACENTO -> acento
        PapelDeCor.SOBRE_ACENTO -> sobreAcento
        PapelDeCor.DESTAQUE -> destaque
        PapelDeCor.CITACAO -> citacao
        PapelDeCor.ESCRITA -> escrita
    }
}

private const val BRANCO_PURO = 0xFFFFFFFF
private const val VERMELHO_JORNAL = 0xFFB00020

private val PAPEL = Paleta(
    fundo = 0xFFFCFBF8,
    superficie = 0xFFF6F1E9,
    cartao = 0xFFFFFDF8,
    divisor = 0xFFE3D9CA,
    textoPrimario = 0xFF333333,
    textoSecundario = 0xFF59534A,
    acento = VERMELHO_JORNAL,
    sobreAcento = BRANCO_PURO,
    destaque = 0xFFF2DE9B,
    citacao = 0xFF8C1C24,
    escrita = 0xFF1F1F1F
)

private val BRANCO = Paleta(
    fundo = BRANCO_PURO,
    superficie = 0xFFF5F5F5,
    cartao = 0xFFFAFAFA,
    divisor = 0xFFE0E0E0,
    textoPrimario = 0xFF333333,
    textoSecundario = 0xFF59534A,
    acento = VERMELHO_JORNAL,
    sobreAcento = BRANCO_PURO,
    destaque = 0xFFFBE7A1,
    citacao = 0xFF8C1C24,
    escrita = 0xFF1F1F1F
)

private val SEPIA = Paleta(
    fundo = 0xFFF4ECD8,
    superficie = 0xFFEDE3CB,
    cartao = 0xFFF8F1E0,
    divisor = 0xFFD8C9A8,
    textoPrimario = 0xFF43382C,
    textoSecundario = 0xFF6B5D4F,
    acento = 0xFFA51D2D,
    sobreAcento = BRANCO_PURO,
    destaque = 0xFFE9CF83,
    citacao = 0xFF7E1A22,
    escrita = 0xFF2E251C
)

/** Um só escuro: o `#121212` do esquema genérico e o `#1B1B1B` de leitura fundiram-se (ADR 0001). */
private val ESCURO = Paleta(
    fundo = 0xFF1B1B1B,
    superficie = 0xFF2A2A2A,
    cartao = 0xFF242424,
    divisor = 0xFF454545,
    textoPrimario = 0xFFE3E0DB,
    textoSecundario = 0xFFA8A29A,
    acento = 0xFFFF8A80,
    sobreAcento = 0xFF3B0A0A,
    destaque = 0xFF5A4A1C,
    // Um vermelho escuro não passa AA sobre o fundo escuro (#9): fica um tom mais claro.
    citacao = 0xFFE57373,
    escrita = 0xFFF0EDE8,
    escura = true
)

/** Resolve todos os papéis de cor de uma Aparência. */
fun paleta(aparencia: ReaderTheme): Paleta = when (aparencia) {
    ReaderTheme.PAPER -> PAPEL
    ReaderTheme.WHITE -> BRANCO
    ReaderTheme.SEPIA -> SEPIA
    ReaderTheme.DARK -> ESCURO
}
