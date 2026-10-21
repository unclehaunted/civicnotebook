package pt.avante.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Seam: o resolver do Espaçamento (#12), puro. Os valores esperados são os confirmados no
 * veredito de #7: entrelinha 1,35 / 1,5 / 1,65 (−0,05 em Grande e Extra grande), parágrafo
 * ½ / ¾ / 1 linha, coluna de 33 × corpo. Uma linha = corpo × entrelinha.
 */
class ArticleSpacingTest {

    private fun resolve(size: ReaderFontSize, spacing: ReaderSpacing) = ArticleSpacing.resolve(size, spacing)

    @Test
    fun `Normal em Medio da entrelinha 1,5 e parágrafo de três quartos de linha`() {
        val medioNormal = resolve(ReaderFontSize.MEDIUM, ReaderSpacing.NORMAL)

        assertEquals(1.5f, medioNormal.lineHeight, DELTA)
        assertEquals(20.25f, medioNormal.paragraphSp, DELTA) // 18 × 1,5 × ¾
    }

    /** Tabela trabalhada à mão: Tamanho, Espaçamento, entrelinha, parágrafo em sp. */
    private val table = listOf(
        Row(ReaderFontSize.SMALL, ReaderSpacing.COMPACT, 1.35f, 10.8f),
        Row(ReaderFontSize.SMALL, ReaderSpacing.NORMAL, 1.5f, 18f),
        Row(ReaderFontSize.SMALL, ReaderSpacing.AIRY, 1.65f, 26.4f),
        Row(ReaderFontSize.MEDIUM, ReaderSpacing.COMPACT, 1.35f, 12.15f),
        Row(ReaderFontSize.MEDIUM, ReaderSpacing.NORMAL, 1.5f, 20.25f),
        Row(ReaderFontSize.MEDIUM, ReaderSpacing.AIRY, 1.65f, 29.7f),
        Row(ReaderFontSize.LARGE, ReaderSpacing.COMPACT, 1.3f, 13.65f),
        Row(ReaderFontSize.LARGE, ReaderSpacing.NORMAL, 1.45f, 22.8375f),
        Row(ReaderFontSize.LARGE, ReaderSpacing.AIRY, 1.6f, 33.6f),
        Row(ReaderFontSize.XLARGE, ReaderSpacing.COMPACT, 1.3f, 15.6f),
        Row(ReaderFontSize.XLARGE, ReaderSpacing.NORMAL, 1.45f, 26.1f),
        Row(ReaderFontSize.XLARGE, ReaderSpacing.AIRY, 1.6f, 38.4f)
    )

    @Test
    fun `cada Espaçamento em cada Tamanho, com menos 0,05 de entrelinha em Grande e Extra grande`() {
        assertEquals(ReaderFontSize.entries.size * ReaderSpacing.entries.size, table.size)
        table.forEach { row ->
            val resolved = resolve(row.size, row.spacing)
            assertEquals("entrelinha $row", row.lineHeight, resolved.lineHeight, DELTA)
            assertEquals("parágrafo $row", row.paragraphSp, resolved.paragraphSp, DELTA)
        }
    }

    @Test
    fun `a coluna tem no máximo 33 vezes o corpo, em cada Tamanho e com qualquer Espaçamento`() {
        val expected = mapOf(
            ReaderFontSize.SMALL to 528f,
            ReaderFontSize.MEDIUM to 594f,
            ReaderFontSize.LARGE to 693f,
            ReaderFontSize.XLARGE to 792f
        )
        assertEquals(ReaderFontSize.entries.toSet(), expected.keys)
        expected.forEach { (size, widthSp) ->
            ReaderSpacing.entries.forEach { spacing ->
                assertEquals("$size $spacing", widthSp, resolve(size, spacing).columnMaxWidthSp, DELTA)
            }
        }
    }

    // Veredito de #7: a Citação tem cerca de 2 % mais espaço antes e 2 % menos depois do
    // que o espaço entre parágrafos (leitura provisória × 1,02 e × 0,98).
    @Test
    fun `a Citação tem 2 % mais espaço antes e 2 % menos depois do que um parágrafo`() {
        val medioNormal = resolve(ReaderFontSize.MEDIUM, ReaderSpacing.NORMAL) // parágrafo 20,25
        assertEquals(20.655f, medioNormal.beforeQuoteSp, DELTA)
        assertEquals(19.845f, medioNormal.afterQuoteSp, DELTA)

        val extraGrandeArejado = resolve(ReaderFontSize.XLARGE, ReaderSpacing.AIRY) // parágrafo 38,4
        assertEquals(39.168f, extraGrandeArejado.beforeQuoteSp, DELTA)
        assertEquals(37.632f, extraGrandeArejado.afterQuoteSp, DELTA)
    }

    // #13 e veredito de #7: a Citação recua 1,5 em, que segue o Tamanho e não o Espaçamento.
    @Test
    fun `a Citação recua 1,5 vezes o corpo em cada Tamanho`() {
        val expected = mapOf(
            ReaderFontSize.SMALL to 24f,
            ReaderFontSize.MEDIUM to 27f,
            ReaderFontSize.LARGE to 31.5f,
            ReaderFontSize.XLARGE to 36f
        )
        assertEquals(ReaderFontSize.entries.toSet(), expected.keys)
        expected.forEach { (size, indentSp) ->
            ReaderSpacing.entries.forEach { spacing ->
                assertEquals("$size $spacing", indentSp, resolve(size, spacing).quoteIndentSp, DELTA)
            }
        }
    }

    // #13, como no protótipo aprovado em #7: dois parágrafos antes do subtítulo, um depois.
    @Test
    fun `o subtítulo tem dois parágrafos antes e um depois`() {
        val medioNormal = resolve(ReaderFontSize.MEDIUM, ReaderSpacing.NORMAL) // parágrafo 20,25
        assertEquals(40.5f, medioNormal.beforeSubheadingSp, DELTA)
        assertEquals(20.25f, medioNormal.afterSubheadingSp, DELTA)

        val pequenoCompacto = resolve(ReaderFontSize.SMALL, ReaderSpacing.COMPACT) // parágrafo 10,8
        assertEquals(21.6f, pequenoCompacto.beforeSubheadingSp, DELTA)
        assertEquals(10.8f, pequenoCompacto.afterSubheadingSp, DELTA)
    }

    @Test
    fun `depois da Pergunta fica meio espaço de parágrafo, colado à resposta`() {
        assertEquals(10.125f, resolve(ReaderFontSize.MEDIUM, ReaderSpacing.NORMAL).afterQuestionSp, DELTA)
        assertEquals(5.4f, resolve(ReaderFontSize.SMALL, ReaderSpacing.COMPACT).afterQuestionSp, DELTA)
    }

    private data class Row(
        val size: ReaderFontSize,
        val spacing: ReaderSpacing,
        val lineHeight: Float,
        val paragraphSp: Float
    )
}

private const val DELTA = 0.001f
