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
import pt.avante.app.theme.OriginalRed

@Composable
fun AvanteMasthead(
    editionLabel: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tagline
        Text(
            text = "PROLETÁRIOS DE TODOS OS PAÍSES, UNI-VOS!",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = OriginalRed,
            modifier = Modifier.padding(bottom = 8.dp)
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
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_avante), // Still using ic_avante vector unless a PNG was provided
                    contentDescription = "Avante!",
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
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = OriginalRed.copy(alpha = 0.2f), thickness = 2.dp)
    }
}
