package pt.avante.app.data

/**
 * Espaçamento (#12): o ar entre linhas e entre parágrafos do artigo, em proporção ao
 * tamanho do texto. Valores confirmados no veredito de #7.
 *
 * @property lineHeight entrelinha, em múltiplos do corpo.
 * @property paragraphInLines espaço entre parágrafos, em linhas (uma linha = corpo × entrelinha).
 */
enum class ReaderSpacing(val label: String, val lineHeight: Float, val paragraphInLines: Float) {
    COMPACT("Compacto", COMPACT_LINE_HEIGHT, HALF_LINE),
    NORMAL("Normal", NORMAL_LINE_HEIGHT, THREE_QUARTERS_OF_A_LINE),
    AIRY("Arejado", AIRY_LINE_HEIGHT, ONE_LINE)
}

private const val COMPACT_LINE_HEIGHT = 1.35f
private const val NORMAL_LINE_HEIGHT = 1.5f
private const val AIRY_LINE_HEIGHT = 1.65f
private const val HALF_LINE = 0.5f
private const val THREE_QUARTERS_OF_A_LINE = 0.75f
private const val ONE_LINE = 1f

/**
 * O Espaçamento resolvido para um Tamanho: valores puros, em sp ou em múltiplos do corpo,
 * que a UI só aplica. Os tamanhos de letra são das escalas (#11); daqui saem só as medidas
 * verticais e a largura da coluna.
 */
data class ArticleSpacing(
    /** Entrelinha do corpo, das Perguntas e das Citações, em múltiplos do tamanho do texto. */
    val lineHeight: Float,
    /** Espaço entre parágrafos, em sp. */
    val paragraphSp: Float,
    /** Espaço entre a Pergunta e a resposta, em sp. */
    val afterQuestionSp: Float,
    /** Espaço entre o bloco anterior e a Citação, em sp. */
    val beforeQuoteSp: Float,
    /** Espaço entre a Citação e o bloco seguinte, em sp. */
    val afterQuoteSp: Float,
    /** Espaço entre o bloco anterior e o subtítulo, em sp. */
    val beforeSubheadingSp: Float,
    /** Espaço entre o subtítulo e o texto que ele abre, em sp. */
    val afterSubheadingSp: Float,
    /** Largura máxima da coluna de texto, em sp (cerca de 66 caracteres por linha). */
    val columnMaxWidthSp: Float,
    /** Recuo à esquerda da Citação, em sp: a sua única marca além do itálico e da cor (#13). */
    val quoteIndentSp: Float
) {
    companion object {
        /** Nos Tamanhos grandes a linha já é longa em altura: a entrelinha desce 0,05. */
        private const val LARGE_SIZE_LINE_HEIGHT_DROP = 0.05f
        private val LARGE_SIZES = setOf(ReaderFontSize.LARGE, ReaderFontSize.XLARGE)

        /** Largura máxima da coluna em múltiplos do corpo: 33 em dão cerca de 66 caracteres. */
        private const val COLUMN_WIDTH_IN_BODY_SIZES = 33f

        /** Recuo da Citação em múltiplos do corpo: 1,5 em, proposto no protótipo e confirmado em #7. */
        private const val QUOTE_INDENT_IN_BODY_SIZES = 1.5f

        /**
         * Entrelinha do título e dos subtítulos, em múltiplos do seu tamanho: não segue o
         * Espaçamento. Proposta no protótipo e confirmada em #7.
         */
        const val HEADING_LINE_HEIGHT = 1.2f

        /** Espaço depois da Pergunta, em fração do espaço entre parágrafos (#7). */
        private const val AFTER_QUESTION_IN_PARAGRAPHS = 0.5f

        /*
         * Espaço à volta da Citação, em múltiplos do espaço entre parágrafos. O veredito de #7
         * (último comentário da issue) pede "cerca de 2 % mais antes e 2 % menos depois";
         * × 1,02 e × 0,98 são a leitura provisória. Afinar aqui.
         */
        const val BEFORE_QUOTE_IN_PARAGRAPHS = 1.02f
        const val AFTER_QUOTE_IN_PARAGRAPHS = 0.98f

        /*
         * O subtítulo pertence ao texto que abre: o dobro do espaço antes do que depois.
         * Valores do protótipo aprovado em #7 (dois parágrafos antes, um depois).
         */
        private const val BEFORE_SUBHEADING_IN_PARAGRAPHS = 2f
        private const val AFTER_SUBHEADING_IN_PARAGRAPHS = 1f

        fun resolve(fontSize: ReaderFontSize, spacing: ReaderSpacing): ArticleSpacing {
            val body = fontSize.spValue
            val lineHeight = spacing.lineHeight - if (fontSize in LARGE_SIZES) LARGE_SIZE_LINE_HEIGHT_DROP else 0f
            val paragraph = body * lineHeight * spacing.paragraphInLines
            return ArticleSpacing(
                lineHeight = lineHeight,
                paragraphSp = paragraph,
                afterQuestionSp = paragraph * AFTER_QUESTION_IN_PARAGRAPHS,
                beforeQuoteSp = paragraph * BEFORE_QUOTE_IN_PARAGRAPHS,
                afterQuoteSp = paragraph * AFTER_QUOTE_IN_PARAGRAPHS,
                beforeSubheadingSp = paragraph * BEFORE_SUBHEADING_IN_PARAGRAPHS,
                afterSubheadingSp = paragraph * AFTER_SUBHEADING_IN_PARAGRAPHS,
                columnMaxWidthSp = body * COLUMN_WIDTH_IN_BODY_SIZES,
                quoteIndentSp = body * QUOTE_INDENT_IN_BODY_SIZES
            )
        }
    }
}
