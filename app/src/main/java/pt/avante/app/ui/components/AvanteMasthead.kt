package pt.avante.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pt.avante.app.R
import pt.avante.app.data.Publication
import pt.avante.app.theme.OriginalRed
import pt.avante.app.theme.Spacing
import pt.avante.app.theme.emMaiusculas

@Composable
fun AvanteMasthead(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    publicationSubtitle: String = "Órgão Central do Partido Comunista Português",
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.m, vertical = if (isCompact) Spacing.xxs else Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tagline
        Text(
            text = "PROLETÁRIOS DE TODOS OS PAÍSES, UNI-VOS!",
            style = MaterialTheme.typography.labelSmall.emMaiusculas(),
            fontWeight = FontWeight.Bold,
            color = OriginalRed.copy(alpha = if (isCompact) 0.8f else 1f),
            modifier = Modifier.padding(bottom = if (isCompact) Spacing.xxs else Spacing.xs)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onMenuClick, modifier = Modifier.width(48.dp)) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
            
            // Logo
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(if (isCompact) 60.dp else 56.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_avante),
                    contentDescription = Publication.AVANTE.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            IconButton(onClick = onSearchClick, modifier = Modifier.width(48.dp)) {
                Icon(Icons.Default.Search, contentDescription = "Pesquisar")
            }
        }
        
        // Publication Subtitle
        Text(
            text = publicationSubtitle,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = Spacing.xxs)
        )
        
        Spacer(modifier = Modifier.height(if (isCompact) Spacing.xxs else Spacing.xs))
        Divider(color = OriginalRed.copy(alpha = 0.2f), thickness = 2.dp)
    }
}
