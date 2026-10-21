package pt.avante.app.theme

import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import pt.avante.app.R

/**
 * Cada recurso em res/font abre como fonte. Os ficheiros do IBM Plex Sans eram páginas HTML
 * guardadas com extensão .ttf e ninguém deu por isso (#6).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FontResourcesTest {

    @Test
    fun everyFontResource_loadsAsATypeface() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val fonts = R.font::class.java.fields.map { it.name to it.getInt(null) }

        assertTrue(fonts.isNotEmpty())
        val broken = fonts.filter { (_, id) ->
            runCatching { ResourcesCompat.getFont(context, id) }.getOrNull() == null
        }
        assertTrue("Fontes que não abrem: ${broken.map { it.first }}", broken.isEmpty())
    }
}
