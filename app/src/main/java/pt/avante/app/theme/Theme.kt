package pt.avante.app.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import pt.avante.app.data.Paleta
import pt.avante.app.data.ReaderTheme
import pt.avante.app.data.paleta

/** A paleta da Aparência atual, provida por [OriginalTheme]. */
val LocalPaleta = staticCompositionLocalOf { paleta(ReaderTheme.PAPER) }

/**
 * Os papéis de cor da Aparência atual (ADR 0001), para a UI. Texto e superfícies novos escolhem
 * um papel daqui ou de `MaterialTheme.colorScheme`, que é feito da mesma paleta; nunca uma cor.
 */
object Cores {
    val fundo: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.fundo)
    val superficie: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.superficie)
    val cartao: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.cartao)
    val divisor: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.divisor)
    val textoPrimario: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.textoPrimario)
    val textoSecundario: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.textoSecundario)
    val acento: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.acento)
    val sobreAcento: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.sobreAcento)
    val destaque: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.destaque)
    val citacao: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.citacao)
    val escrita: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.escrita)
    val contorno: Color @Composable @ReadOnlyComposable get() = Color(LocalPaleta.current.contorno)
}

/**
 * Texto e véu pousados sobre uma fotografia: não mudam com a Aparência, porque o fundo é a
 * imagem e não a paleta.
 */
object SobreImagem {
    val texto: Color = Color.White
    val veu: Color = Color.Black
}

/** O esquema Material feito da paleta: os componentes Material ficam na mesma Aparência. */
private fun esquemaMaterial(p: Paleta): ColorScheme {
    val fundo = Color(p.fundo)
    val superficie = Color(p.superficie)
    val cartao = Color(p.cartao)
    val texto = Color(p.textoPrimario)
    val secundario = Color(p.textoSecundario)
    val acento = Color(p.acento)
    val sobreAcento = Color(p.sobreAcento)
    val base = if (p.escura) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = acento,
        onPrimary = sobreAcento,
        primaryContainer = superficie,
        onPrimaryContainer = texto,
        inversePrimary = acento,
        secondary = secundario,
        onSecondary = fundo,
        secondaryContainer = superficie,
        onSecondaryContainer = texto,
        tertiary = Color(p.destaque),
        onTertiary = texto,
        tertiaryContainer = Color(p.destaque),
        onTertiaryContainer = texto,
        background = fundo,
        onBackground = texto,
        surface = fundo,
        onSurface = texto,
        surfaceVariant = superficie,
        onSurfaceVariant = secundario,
        // Sem tinta de elevação: uma superfície elevada não se tinge de vermelho.
        surfaceTint = fundo,
        inverseSurface = texto,
        inverseOnSurface = fundo,
        error = acento,
        onError = sobreAcento,
        errorContainer = superficie,
        onErrorContainer = acento,
        outline = Color(p.contorno),
        outlineVariant = Color(p.divisor),
        surfaceBright = cartao,
        surfaceDim = superficie,
        surfaceContainerLowest = cartao,
        surfaceContainerLow = cartao,
        surfaceContainer = superficie,
        surfaceContainerHigh = superficie,
        surfaceContainerHighest = superficie
    )
}

/**
 * O tema da app inteira, na Aparência escolhida nas Definições (ADR 0001). As cores dinâmicas
 * do sistema ficam de fora: apagariam a identidade do jornal. Do sistema só se lê o modo escuro,
 * para a Automática.
 */
@Composable
fun OriginalTheme(
    aparencia: ReaderTheme = ReaderTheme.PAPER,
    content: @Composable () -> Unit,
) {
    val paleta = paleta(aparencia, sistemaEscuro = isSystemInDarkTheme())
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val barras = WindowCompat.getInsetsController(window, view)
            barras.isAppearanceLightStatusBars = !paleta.escura
            barras.isAppearanceLightNavigationBars = !paleta.escura
        }
    }
    CompositionLocalProvider(LocalPaleta provides paleta) {
        MaterialTheme(
            colorScheme = remember(paleta) { esquemaMaterial(paleta) },
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
