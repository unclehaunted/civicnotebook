package pt.avante.app.ui.components.cards

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.avante.app.R
import pt.avante.app.data.ArticleSummary
import pt.avante.app.theme.*
import pt.avante.app.ui.components.Pill
import pt.avante.app.ui.presentation.ArticleImagePolicy
import pt.avante.app.ui.presentation.ArticleCardMetrics
import pt.avante.app.ui.settings.LocalReaderSettings

@Composable
fun ArticleThumbnail(
    heroImageUrl: String?,
    imagePolicy: ArticleImagePolicy,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small
) {
    if (imagePolicy == ArticleImagePolicy.NO_IMAGE) return
    if (!LocalReaderSettings.current.showImages) return

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
            val displayAuthor = author.ifBlank { pt.avante.app.data.DEFAULT_ARTICLE_AUTHOR }
            Pill(
                text = displayAuthor,
                containerColor = WarmPaper,
                contentColor = TextSecondary,
                horizontalPadding = Spacing.s
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)
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

@Composable
fun NotesBookmarkOverlay(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.semantics { contentDescription = "Artigo com notas" },
        shape = MaterialTheme.shapes.medium.copy(topStart = ZeroCornerSize, bottomEnd = ZeroCornerSize),
        color = Color(0xFFFFD54F), // Yellow ribbon
        shadowElevation = 2.dp
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Edit,
            contentDescription = null, // Semantics already on Surface
            tint = Color(0xFF5D4037), // Dark brown/ink
            modifier = Modifier.padding(Spacing.xs).size(16.dp)
        )
    }
}
