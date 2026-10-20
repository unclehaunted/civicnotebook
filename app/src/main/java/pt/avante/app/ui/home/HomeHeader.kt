package pt.avante.app.ui.home

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.SoftDivider
import pt.avante.app.theme.TextPrimary
import pt.avante.app.theme.WarmPaper
import pt.avante.app.ui.components.AvanteMasthead

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

    Surface(
        shadowElevation = 2.dp,
        color = WarmPaper
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            tint = OriginalRed.copy(alpha = if (isCompact) 0.8f else 1f),
                            modifier = Modifier.size(if (isCompact) 12.dp else 14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = editionText,
                            style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                            color = if (isCompact) TextPrimary.copy(alpha = 0.8f) else TextPrimary,
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
            
            HorizontalDivider(color = SoftDivider, thickness = 1.dp)
        }
    }
}

@Composable
fun SegmentedIconButton(icon: ImageVector, selected: Boolean, onClick: () -> Unit, isFirst: Boolean = false, isLast: Boolean = false, isCompact: Boolean = false) {
    val shape = when {
        isFirst -> RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
        isLast -> RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp)
        else -> RoundedCornerShape(0.dp)
    }
    
    Surface(
        color = if (selected) OriginalRed else Color.Transparent,
        shape = shape,
        border = BorderStroke(1.dp, OriginalRed),
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else OriginalRed,
            modifier = Modifier
                .padding(
                    horizontal = if (isCompact) 8.dp else 10.dp,
                    vertical = if (isCompact) 4.dp else 5.dp
                )
                .size(if (isCompact) 16.dp else 18.dp)
        )
    }
}
