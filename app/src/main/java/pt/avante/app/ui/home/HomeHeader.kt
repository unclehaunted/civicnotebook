package pt.avante.app.ui.home

import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.Cores
import pt.avante.app.ui.components.AvanteMasthead
import pt.avante.app.ui.components.MastheadMetrics

enum class HomeLayoutMode {
    STACK, GRID, LIST
}

@Composable
fun HomeHeader(
    editionText: String,
    layoutMode: HomeLayoutMode,
    onLayoutModeChange: (HomeLayoutMode) -> Unit,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val viewport = pt.avante.app.ui.layout.LocalAppViewport.current
    val isCompact = viewport.isCompactWidth
    val medidas = MastheadMetrics.de(isCompact)

    Surface(
        shadowElevation = 2.dp,
        color = Cores.fundo
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AvanteMasthead(
                onMenuClick = onMenuClick,
                onSearchClick = onSearchClick,
                isCompact = isCompact
            )
            
            if (editionText.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = medidas.margemLateral, vertical = medidas.margemVerticalDaEdicao),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Edition Badge (left side)
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Cores.acento.copy(alpha = if (isCompact) 0.8f else 1f),
                            modifier = Modifier.size(medidas.iconeDaEdicao)
                        )
                        Spacer(modifier = Modifier.width(medidas.folgaDoIconeDaEdicao))
                        Text(
                            text = editionText,
                            style = medidas.edicao,
                            color = if (isCompact) Cores.textoPrimario.copy(alpha = 0.8f) else Cores.textoPrimario,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Layout Toggle (right side)
                    Row {
                        SegmentedIconButton(
                            icon = Icons.AutoMirrored.Filled.ViewList,
                            selected = layoutMode == HomeLayoutMode.LIST,
                            onClick = { onLayoutModeChange(HomeLayoutMode.LIST) },
                            isFirst = true,
                            isCompact = isCompact
                        )
                        SegmentedIconButton(
                            icon = Icons.Default.ViewCarousel,
                            selected = layoutMode == HomeLayoutMode.STACK,
                            onClick = { onLayoutModeChange(HomeLayoutMode.STACK) },
                            isFirst = false,
                            isLast = false,
                            isCompact = isCompact
                        )
                        SegmentedIconButton(
                            icon = Icons.Default.GridView,
                            selected = layoutMode == HomeLayoutMode.GRID,
                            onClick = { onLayoutModeChange(HomeLayoutMode.GRID) },
                            isLast = true,
                            isCompact = isCompact
                        )
                    }
                }
            }
            
            HorizontalDivider(color = Cores.divisor, thickness = 1.dp)
        }
    }
}

@Composable
fun SegmentedIconButton(icon: ImageVector, selected: Boolean, onClick: () -> Unit, isFirst: Boolean = false, isLast: Boolean = false, isCompact: Boolean = false) {
    val medidas = MastheadMetrics.de(isCompact)
    val canto = medidas.cantoDoSegmento
    val shape = when {
        isFirst -> RoundedCornerShape(topStart = canto, bottomStart = canto)
        isLast -> RoundedCornerShape(topEnd = canto, bottomEnd = canto)
        else -> RectangleShape
    }
    
    Surface(
        color = if (selected) Cores.acento else Color.Transparent,
        shape = shape,
        border = BorderStroke(1.dp, Cores.acento),
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Cores.sobreAcento else Cores.acento,
            modifier = Modifier
                .padding(
                    horizontal = medidas.folgaLateralDoSegmento,
                    vertical = medidas.folgaVerticalDoSegmento
                )
                .size(medidas.iconeDoSegmento)
        )
    }
}
