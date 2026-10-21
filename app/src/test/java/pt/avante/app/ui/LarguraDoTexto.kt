package pt.avante.app.ui

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.text.TextLayoutResult
import androidx.core.content.res.ResourcesCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlin.math.abs

/*
 * Que ficheiro de fonte desenhou um texto Compose, visto pelas larguras dos glifos.
 * O negrito e o itálico sintetizados desenham sobre o ficheiro regular e ficam com as larguras
 * dele; o ficheiro desenhado (ou a instância wght) tem larguras próprias. Assim a largura
 * distingue o efeito pedido do fallback (CODING_STANDARDS.md, "Distinguir o efeito do recurso").
 */

/** Tolerância, em px, entre a largura medida no Paint e a da composição. */
private const val TOLERANCIA_PX = 1.5f

/** Diferença mínima, em px, para a amostra separar o ficheiro esperado do fallback. */
private const val SEPARACAO_MINIMA_PX = 3 * TOLERANCIA_PX

/** O texto composto deste nó (o transformado, num campo com VisualTransformation). */
fun SemanticsNodeInteraction.composicao(): TextLayoutResult {
    val layouts = mutableListOf<TextLayoutResult>()
    fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
    return layouts.single()
}

/** Largura, em px, de [trecho] dentro do texto composto (numa só linha). */
fun TextLayoutResult.larguraDe(trecho: String): Float {
    val inicio = layoutInput.text.text.indexOf(trecho)
    check(inicio >= 0) { "\"$trecho\" não está em \"${layoutInput.text.text}\"" }
    check(getLineForOffset(inicio) == getLineForOffset(inicio + trecho.length - 1)) { "\"$trecho\" parte a linha" }
    return getHorizontalPosition(inicio + trecho.length, true) - getHorizontalPosition(inicio, true)
}

/** Largura, em px, de [texto] desenhado com o ficheiro de fonte [fonte] a [tamanhoSp]. */
fun Context.larguraComFicheiro(fonte: Int, tamanhoSp: Float, texto: String): Float {
    val typeface: Typeface = ResourcesCompat.getFont(this, fonte)!!
    return Paint().apply {
        this.typeface = typeface
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, tamanhoSp, resources.displayMetrics)
    }.measureText(texto)
}

/**
 * [trecho] foi composto com o ficheiro [esperado] a [tamanhoSp], e não com [fallback]
 * (o regular que um estilo sintetizado, ou outra face, teria usado).
 */
fun Context.assertDesenhadoCom(
    composicao: TextLayoutResult,
    trecho: String,
    tamanhoSp: Float,
    esperado: Int,
    fallback: Int
) {
    val medido = composicao.larguraDe(trecho)
    val doEsperado = larguraComFicheiro(esperado, tamanhoSp, trecho)
    val doFallback = larguraComFicheiro(fallback, tamanhoSp, trecho)
    assertTrue(
        "a amostra \"$trecho\" não separa os ficheiros ($doEsperado vs $doFallback px)",
        abs(doEsperado - doFallback) > SEPARACAO_MINIMA_PX
    )
    assertEquals(
        "\"$trecho\": esperado $doEsperado px (fallback $doFallback px)",
        doEsperado,
        medido,
        TOLERANCIA_PX
    )
}
