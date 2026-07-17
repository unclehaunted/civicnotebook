package pt.avante.app.ui.components.cards

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.avante.app.R
import pt.avante.app.data.ArticleSummary
import pt.avante.app.theme.*
import pt.avante.app.ui.presentation.ArticleImagePolicy
import pt.avante.app.ui.presentation.ArticleCardMetrics

@Composable
fun ArticleThumbnail(
    heroImageUrl: String?,
    imagePolicy: ArticleImagePolicy,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    if (imagePolicy == ArticleImagePolicy.NO_IMAGE) return

    val shape = RoundedCornerShape(cornerRadius)

    if (imagePolicy == ArticleImagePolicy.OPINION_AH_PLACEHOLDER) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(WarmPaper),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ah_placeholder),
                contentDescription = "Opinião Placeholder",
                modifier = Modifier.fillMaxSize(0.6f)
            )
        }
    } else {
        if (!heroImageUrl.isNullOrBlank() && heroImageUrl != "LOCAL_SYMBOL") {
            AsyncImage(
                model = heroImageUrl,
                contentDescription = "Imagem do Artigo",
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(shape)
            )
        }
    }
}

@Composable
fun ArticleCardFooter(
    article: ArticleSummary,
    author: String,
    presentation: pt.avante.app.ui.presentation.ArticleCardPresentation,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = pt.avante.app.theme.MutedGreen
            ) {
                Text(
                    text = "Ler Artigo",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = pt.avante.app.theme.WarmPaper
            ) {
                Text(
                    text = author,
                    style = MaterialTheme.typography.labelSmall,
                    color = pt.avante.app.theme.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!article.isFree) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(pt.avante.app.theme.OriginalRed, androidx.compose.foundation.shape.CircleShape)
                        .semantics { contentDescription = "Artigo exclusivo" }
                )
            }
            
            if (presentation.showFavourite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Guardar",
                    tint = Color.LightGray,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { /* TODO: Guardar artigo */ }
                )
            }
        }
    }
}
