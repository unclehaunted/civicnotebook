package pt.avante.app.ui.reader

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import pt.avante.app.ui.presentation.ArticleAuthorFormatter

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

@Composable
fun ReaderProvenance(
    publication: pt.avante.app.data.Publication,
    issueNumber: String?,
    author: String?,
    currentIssueNumber: String?,
    subcategoryLabel: String? = null,
    hasNotes: Boolean = false,
    hasHighlights: Boolean = false,
    textColor: Color = pt.avante.app.theme.Cores.textoSecundario,
    modifier: Modifier = Modifier
) {
    val formatted = pt.avante.app.ui.components.ProvenanceFormatter.formatProvenance(
        publication = publication.displayName,
        issueNumber = issueNumber,
        author = author,
        currentIssueNumber = currentIssueNumber,
        subcategory = subcategoryLabel,
        hasNotes = hasNotes,
        hasHighlights = hasHighlights
    )
    
    Text(
        text = formatted,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        modifier = modifier
    )
}
