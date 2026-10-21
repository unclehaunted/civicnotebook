package pt.avante.app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Seam: as preferências como o Leitor as reencontra ao reabrir a app (um store novo). */
@RunWith(RobolectricTestRunner::class)
class ReaderSettingsStoreTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val prefs = context.getSharedPreferences("pt.avante.app.reader_settings", Context.MODE_PRIVATE)

    private fun reabrir() = ReaderSettingsStore(context).settings.value

    @Test
    fun semPreferencias_abreEmClassicoEIaWriterDuo() {
        assertEquals(TiposDeLetra.CLASSICO, reabrir().tiposDeLetra)
        assertEquals(Escrita.IA_WRITER_DUO, reabrir().escrita)
    }

    // User story 4: quem escolheu Atkinson Hyperlegible antes da #10 reencontra-o como Legível.
    @Test
    fun fonteAntigaAtkinson_abreComoLegivel() {
        prefs.edit().putString("reader_font", "ATKINSON_HYPERLEGIBLE").commit()

        assertEquals(TiposDeLetra.LEGIVEL, reabrir().tiposDeLetra)
    }

    // #6: o IBM Plex Sans saiu da lista; quem o tinha escolhido passa a Clássico.
    @Test
    fun fonteAntigaIbmPlexSans_abreComoClassico() {
        prefs.edit().putString("reader_font", "IBM_PLEX_SANS").commit()

        assertEquals(TiposDeLetra.CLASSICO, reabrir().tiposDeLetra)
    }

    @Test
    fun tiposDeLetraEscolhidos_ganhamAFonteAntiga() {
        prefs.edit().putString("reader_font", "ATKINSON_HYPERLEGIBLE").commit()

        ReaderSettingsStore(context).setTiposDeLetra(TiposDeLetra.MODERNO)

        assertEquals(TiposDeLetra.MODERNO, reabrir().tiposDeLetra)
    }

    @Test
    fun tiposDeLetraEscolhidos_ficamGuardados() {
        val store = ReaderSettingsStore(context)
        store.setTiposDeLetra(TiposDeLetra.LIVRO)

        assertEquals(TiposDeLetra.LIVRO, store.settings.value.tiposDeLetra)
        assertEquals(TiposDeLetra.LIVRO, reabrir().tiposDeLetra)
    }

    @Test
    fun escritaEscolhida_ficaGuardada() {
        val store = ReaderSettingsStore(context)
        store.setEscrita(Escrita.SOURCE_SANS)

        assertEquals(Escrita.SOURCE_SANS, store.settings.value.escrita)
        assertEquals(Escrita.SOURCE_SANS, reabrir().escrita)
    }

    @Test
    fun valorGuardadoDesconhecido_abreNosValoresPorOmissao() {
        prefs.edit()
            .putString("reader_tipos_de_letra", "INVERSO")
            .putString("reader_escrita", "COMIC_SANS")
            .commit()

        assertEquals(TiposDeLetra.CLASSICO, reabrir().tiposDeLetra)
        assertEquals(Escrita.IA_WRITER_DUO, reabrir().escrita)
    }

    // #5: uma instalação nova segue o modo do sistema.
    @Test
    fun semAparenciaGuardada_abreEmAutomatica() {
        assertEquals(ReaderTheme.AUTOMATIC, reabrir().theme)
    }

    // Quem já escolheu uma Aparência antes da Automática existir reencontra-a.
    @Test
    fun aparenciaGuardada_ficaAMesma() {
        prefs.edit().putString("reader_theme", "SEPIA").commit()

        assertEquals(ReaderTheme.SEPIA, reabrir().theme)
    }

    @Test
    fun aparenciaGuardadaDesconhecida_abreEmAutomatica() {
        prefs.edit().putString("reader_theme", "NEON").commit()

        assertEquals(ReaderTheme.AUTOMATIC, reabrir().theme)
    }

    // #12: o Espaçamento escolhido sobrevive a reabrir a app.
    @Test
    fun chosenSpacing_survivesReopening() {
        val store = ReaderSettingsStore(context)
        store.setSpacing(ReaderSpacing.AIRY)

        assertEquals(ReaderSpacing.AIRY, store.settings.value.spacing)
        assertEquals(ReaderSpacing.AIRY, ReaderSettingsStore(context).settings.value.spacing)
    }

    @Test
    fun noStoredSpacing_opensAsNormal() {
        assertEquals(ReaderSpacing.NORMAL, ReaderSettingsStore(context).settings.value.spacing)
    }

    @Test
    fun unknownStoredSpacing_opensAsNormal() {
        context.getSharedPreferences("pt.avante.app.reader_settings", Context.MODE_PRIVATE)
            .edit().putString("reader_spacing", "APERTADO").commit()

        assertEquals(ReaderSpacing.NORMAL, ReaderSettingsStore(context).settings.value.spacing)
    }
}
