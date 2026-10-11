package pt.avante.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.avante.app.data.Publication
import pt.avante.app.ui.components.CategoryRubricPill
import pt.avante.app.ui.presentation.ArticleTaxonomyFormatter
import pt.avante.app.ui.settings.LocalReaderSettings

@Composable
fun ReaderHero(
    title: String,
    categoryLabel: String?,
    subcategoryLabel: String?,
    publication: Publication,
    issueNumber: String?,
    author: String,
    displayImage: String?,
    currentIssueNumber: String?,
    modifier: Modifier = Modifier
) {
    val settings = LocalReaderSettings.current
    val hasImage = settings.showImages && !displayImage.isNullOrBlank()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (hasImage) Color.Transparent else Color(settings.theme.backgroundColor))
    ) {
        if (hasImage) {
            // Big photo covering the head space
            AsyncImage(
                model = displayImage,
                contentDescription = "Imagem do Artigo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            
            // Gradient overlay to ensure white text is readable
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 200f
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            if (!categoryLabel.isNullOrBlank()) {
                CategoryRubricPill(
                    formattedTaxonomy = ArticleTaxonomyFormatter.formatPillText(
                        categoryLabel = categoryLabel,
                        subcategoryLabel = subcategoryLabel
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = if (hasImage) Color.White else Color(settings.theme.textColor),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
