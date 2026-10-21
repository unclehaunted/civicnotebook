package pt.avante.app.ui.components

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * As proporções do masthead e do cabeçalho da Home à volta dele (linha da Edição, botões de
 * disposição), compactas e expandidas.
 *
 * O masthead é a identidade da publicação (#20): as proporções foram fixadas pelo mantenedor
 * (2026-10-21) e ficam fora das escalas de espaçamento, raio e tipo. A spec #49 deixou o
 * masthead fora da barra de título comum. Os valores são os do masthead de 19 de julho, commit
 * a8bf011 ("style: refine compact masthead hierarchy"), anterior a #49/#5; só as cores seguem a
 * Aparência (os papéis de `Cores`). As letras são a fonte do
 * sistema porque era essa que desenhava: os ficheiros IBM Plex de então não eram fontes.
 * `scripts/check-design-tokens.sh` isenta este ficheiro; `MastheadProporcoesTest` fixa a
 * geometria desenhada.
 */
object MastheadMetrics {

    /** Um conjunto de proporções: o compacto (janela estreita) ou o expandido. */
    data class Proporcoes(
        val margemLateral: Dp,
        val margemVerticalDoMasthead: Dp,
        val divisa: TextStyle,
        val folgaSobDivisa: Dp,
        val larguraDoBotaoDeIcone: Dp,
        val alturaDoLogotipo: Dp,
        val subtitulo: TextStyle,
        val folgaSobreSubtitulo: Dp,
        val folgaSobSubtitulo: Dp,
        val espessuraDoFilete: Dp,
        val margemVerticalDaEdicao: Dp,
        val iconeDaEdicao: Dp,
        val folgaDoIconeDaEdicao: Dp,
        val edicao: TextStyle,
        val cantoDoSegmento: Dp,
        val folgaLateralDoSegmento: Dp,
        val folgaVerticalDoSegmento: Dp,
        val iconeDoSegmento: Dp,
    )

    private val rotuloPequeno = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )

    private val rotuloMedio = rotuloPequeno.copy(fontSize = 11.sp, lineHeight = 16.sp)

    val compacto = Proporcoes(
        margemLateral = 16.dp,
        margemVerticalDoMasthead = 4.dp,
        divisa = rotuloPequeno.copy(fontSize = 9.sp, lineHeight = 12.sp),
        folgaSobDivisa = 2.dp,
        larguraDoBotaoDeIcone = 48.dp,
        alturaDoLogotipo = 60.dp,
        subtitulo = rotuloPequeno.copy(fontSize = 10.sp, lineHeight = 12.sp),
        folgaSobreSubtitulo = 4.dp,
        folgaSobSubtitulo = 4.dp,
        espessuraDoFilete = 2.dp,
        margemVerticalDaEdicao = 8.dp,
        iconeDaEdicao = 12.dp,
        folgaDoIconeDaEdicao = 6.dp,
        edicao = rotuloPequeno,
        cantoDoSegmento = 14.dp,
        folgaLateralDoSegmento = 8.dp,
        folgaVerticalDoSegmento = 4.dp,
        iconeDoSegmento = 16.dp,
    )

    val expandido = Proporcoes(
        margemLateral = 16.dp,
        margemVerticalDoMasthead = 8.dp,
        divisa = rotuloPequeno,
        folgaSobDivisa = 8.dp,
        larguraDoBotaoDeIcone = 48.dp,
        alturaDoLogotipo = 56.dp,
        subtitulo = rotuloMedio,
        folgaSobreSubtitulo = 4.dp,
        folgaSobSubtitulo = 8.dp,
        espessuraDoFilete = 2.dp,
        margemVerticalDaEdicao = 8.dp,
        iconeDaEdicao = 14.dp,
        folgaDoIconeDaEdicao = 6.dp,
        edicao = rotuloMedio,
        cantoDoSegmento = 14.dp,
        folgaLateralDoSegmento = 10.dp,
        folgaVerticalDoSegmento = 5.dp,
        iconeDoSegmento = 18.dp,
    )

    fun de(compacto: Boolean): Proporcoes = if (compacto) this.compacto else expandido
}
