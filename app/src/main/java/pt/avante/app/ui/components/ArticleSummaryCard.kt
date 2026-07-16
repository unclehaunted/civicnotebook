package pt.avante.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.avante.app.R
import pt.avante.app.data.ArticleSummary
import pt.avante.app.theme.*

@Composable
fun LeadLargeArticleCard(article: ArticleSummary, onClick: () -> Unit) {
    if (article.heroImageUrl != null) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SoftDivider),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.outlinedCardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                if (article.heroImageUrl == "LOCAL_SYMBOL") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_avante),
                            contentDescription = "Avante Logo",
                            tint = Color.Black,
                            modifier = Modifier.fillMaxSize(0.6f)
                        )
                    }
                } else {
                    AsyncImage(
                        model = article.heroImageUrl,
                        contentDescription = "Imagem do Artigo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(220.dp)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Surface(
                        color = OriginalRed,
                        shape = CircleShape
                    ) {
                        Text(
                            text = (article.categoryLabel ?: "Avante!").uppercase(),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    article.subcategoryLabel?.let { subcategory ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subcategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = OriginalRed,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = article.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ArticleCardFooter(article)
                }
            }
        }
    } else {
        TextArticleCard(article, onClick)
    }
}

@Composable
fun FeaturedMediumArticleCard(article: ArticleSummary, onClick: () -> Unit) {
    if (article.heroImageUrl != null) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SoftDivider),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.outlinedCardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                if (article.heroImageUrl == "LOCAL_SYMBOL") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_avante),
                            contentDescription = "Avante Logo",
                            tint = Color.Black,
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                    }
                } else {
                    AsyncImage(
                        model = article.heroImageUrl,
                        contentDescription = "Imagem do Artigo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Surface(
                        color = OriginalRed,
                        shape = CircleShape
                    ) {
                        Text(
                            text = (article.categoryLabel ?: "Avante!").uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    article.subcategoryLabel?.let { subcategory ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subcategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = OriginalRed,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ArticleCardFooter(article)
                }
            }
        }
    } else {
        TextOnlyArticleCard(article, onClick)
    }
}

@Composable
fun StandardSmallArticleCard(article: ArticleSummary, onClick: () -> Unit) {
    if (article.heroImageUrl != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (article.heroImageUrl == "LOCAL_SYMBOL") {
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(90.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_avante),
                            contentDescription = "Avante Logo",
                            tint = Color.Black,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = article.heroImageUrl,
                        contentDescription = "Imagem do Artigo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(90.dp).height(90.dp)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = (article.categoryLabel ?: "Avante!").uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = OriginalRed
                    )
                    article.subcategoryLabel?.let { subcategory ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subcategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    } else {
        TextOnlyArticleCard(article, onClick)
    }
}

@Composable
fun OpinionCompactArticleCard(article: ArticleSummary, onClick: () -> Unit) {
    if (article.heroImageUrl != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (article.heroImageUrl == "LOCAL_SYMBOL") {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_avante),
                            contentDescription = "Avante Logo",
                            tint = Color.Black,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                } else {
                    AsyncImage(
                        model = article.heroImageUrl,
                        contentDescription = "Imagem do Artigo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(100.dp).height(100.dp)
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = (article.categoryLabel ?: "Avante!").uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OriginalRed
                        )
                        article.subcategoryLabel?.let { subcategory ->
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = subcategory,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (article.summary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = article.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    } else {
        TextArticleCard(article, onClick)
    }
}

@Composable
fun TextOnlyArticleCard(article: ArticleSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = OriginalRed,
                shape = CircleShape
            ) {
                Text(
                    text = (article.categoryLabel ?: "Avante!").uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            article.subcategoryLabel?.let { subcategory ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subcategory,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall,
                color = OriginalRed
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            ArticleCardFooter(article)
        }
    }
}

@Composable
fun ArticleCardFooter(article: ArticleSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFF2E7D32)) // Green
            ) {
                Text(
                    text = "Ler artigo",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            if (article.isPremium) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_ahsickle),
                    contentDescription = "Premium Article",
                    tint = OriginalRed,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                color = Color.Transparent,
                shape = CircleShape,
                border = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.2f))
            ) {
                Text(
                    text = article.author ?: "Redacção",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!article.isFree) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(OriginalRed, CircleShape)
                )
            }
            Icon(
                Icons.Default.Star,
                contentDescription = "Guardar",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
