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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import pt.avante.app.R
import pt.avante.app.data.Publication
import pt.avante.app.theme.Cores

/** O masthead da publicação, nas proporções fixas de [MastheadMetrics]. */
@Composable
fun AvanteMasthead(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    publicationSubtitle: String = "Órgão Central do Partido Comunista Português",
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val medidas = MastheadMetrics.de(isCompact)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = medidas.margemLateral, vertical = medidas.margemVerticalDoMasthead),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tagline
        Text(
            text = "PROLETÁRIOS DE TODOS OS PAÍSES, UNI-VOS!",
            style = medidas.divisa,
            fontWeight = FontWeight.Bold,
            color = Cores.acento.copy(alpha = if (isCompact) 0.8f else 1f),
            modifier = Modifier.padding(bottom = medidas.folgaSobDivisa)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onMenuClick, modifier = Modifier.width(medidas.larguraDoBotaoDeIcone)) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }

            // Logo: as letras na cor do texto da Aparência, o acento vermelho por cima, na mesma tela.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(medidas.alturaDoLogotipo),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_avante_letras),
                    contentDescription = Publication.AVANTE.displayName,
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Cores.textoPrimario),
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_avante_acento),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            IconButton(onClick = onSearchClick, modifier = Modifier.width(medidas.larguraDoBotaoDeIcone)) {
                Icon(Icons.Default.Search, contentDescription = "Pesquisar")
            }
        }

        // Publication Subtitle
        Text(
            text = publicationSubtitle,
            style = medidas.subtitulo,
            color = Cores.textoSecundario,
            modifier = Modifier.padding(top = medidas.folgaSobreSubtitulo)
        )

        Spacer(modifier = Modifier.height(medidas.folgaSobSubtitulo))
        Divider(color = Cores.acento.copy(alpha = 0.2f), thickness = medidas.espessuraDoFilete)
    }
}
