package pt.avante.app.theme

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.R

/**
 * As famílias Compose das fontes variáveis desenham o peso pedido (#6). A instância por omissão
 * destes ficheiros é a mais leve: sem os recursos que fixam o wght, regular e negrito saíam Thin.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ComposeFontFamiliesTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun libreFranklin_boldIsHeavierThanRegularAndRegularIsNotThin() =
        assertWeights(LibreFranklin, R.font.libre_franklin_vf)

    @Test
    fun sourceSans_boldIsHeavierThanRegularAndRegularIsNotThin() =
        assertWeights(SourceSans3, R.font.source_sans_vf)

    private fun assertWeights(family: FontFamily, variableFile: Int) {
        val ink = renderInk(
            "regular" to Triple(family, FontWeight.Normal, FontStyle.Normal),
            "bold" to Triple(family, FontWeight.Bold, FontStyle.Normal)
        )
        val lightest = inkOf(ResourcesCompat.getFont(rule.activity, variableFile)!!)
        assertTrue("regular $ink, instância por omissão $lightest", ink.getValue("regular") > lightest * 1.5f)
        assertTrue("negrito $ink", ink.getValue("bold") > ink.getValue("regular") * 1.2f)
    }

    /** A mesma amostra desenhada com o ficheiro variável cru (instância por omissão). */
    private fun inkOf(typeface: Typeface): Int {
        val metrics = rule.activity.resources.displayMetrics
        val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, SIZE_SP, metrics)
        val bitmap = Bitmap.createBitmap((size * 12).toInt(), (size * 2).toInt(), Bitmap.Config.ARGB_8888)
        val paint = Paint().apply {
            this.typeface = typeface
            textSize = size
            color = Color.BLACK
        }
        Canvas(bitmap).drawText(SAMPLE, 0f, size * 1.5f, paint)
        return countInk(bitmap, 0 until bitmap.width, 0 until bitmap.height)
    }

    private fun countInk(bitmap: Bitmap, xs: IntRange, ys: IntRange): Int {
        var count = 0
        for (x in xs) for (y in ys) {
            val pixel = bitmap.getPixel(x, y)
            if (Color.alpha(pixel) > 128 && Color.red(pixel) < 128) count++
        }
        return count
    }

    /** Desenha cada amostra e conta os píxeis pintados dentro dos limites do seu nó. */
    private fun renderInk(vararg samples: Pair<String, Triple<FontFamily, FontWeight, FontStyle>>): Map<String, Int> {
        rule.setContent {
            Column {
                samples.forEach { (tag, style) ->
                    Text(
                        SAMPLE,
                        fontFamily = style.first,
                        fontWeight = style.second,
                        fontStyle = style.third,
                        fontSize = SIZE_SP.sp,
                        modifier = Modifier.testTag(tag)
                    )
                }
            }
        }
        rule.waitForIdle()
        val root = rule.activity.window.decorView
        val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        root.draw(Canvas(bitmap))
        return samples.associate { (tag, _) ->
            val bounds = rule.onNodeWithTag(tag).fetchSemanticsNode().boundsInWindow
            tag to countInk(
                bitmap,
                bounds.left.toInt() until bounds.right.toInt(),
                bounds.top.toInt() until bounds.bottom.toInt()
            )
        }
    }

    private companion object {
        const val SAMPLE = "Avante camaradas"
        const val SIZE_SP = 32f
    }
}
