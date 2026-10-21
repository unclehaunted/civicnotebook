package pt.avante.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReaderSettingsStoreTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    // #6: o IBM Plex Sans saiu da lista; quem o tinha escolhido passa à fonte por omissão (Clássico em #5).
    @Test
    fun storedIbmPlexSans_opensAsLibreFranklin() {
        context.getSharedPreferences("pt.avante.app.reader_settings", Context.MODE_PRIVATE)
            .edit().putString("reader_font", "IBM_PLEX_SANS").commit()

        assertEquals(ReaderFont.LIBRE_FRANKLIN, ReaderSettingsStore(context).settings.value.font)
    }

    @Test
    fun storedFont_isKept() {
        context.getSharedPreferences("pt.avante.app.reader_settings", Context.MODE_PRIVATE)
            .edit().putString("reader_font", "SOURCE_SANS").commit()

        assertEquals(ReaderFont.SOURCE_SANS, ReaderSettingsStore(context).settings.value.font)
    }
}
