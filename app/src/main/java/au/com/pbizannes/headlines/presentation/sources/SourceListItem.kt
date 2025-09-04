package au.com.pbizannes.headlines.presentation.sources

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.com.pbizannes.headlines.domain.models.NewsSource

@Composable
fun SourceListItem(
    itemState: SourceItemUiState,
    onCheckedChange: (sourceId: String, isSelected: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val source = itemState.source
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                source.id?.let { id -> onCheckedChange(id, !itemState.isSelected) }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = source.description,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(16.dp))
        Checkbox(
            checked = itemState.isSelected,
            onCheckedChange = { isChecked ->
                source.id?.let { id -> onCheckedChange(id, isChecked) }
            },
            enabled = source.id != null
        )
    }
    Divider()
}

@Preview(showBackground = true, name = "Source List Item - Selected")
@Composable
fun SourceListItemSelectedPreview() {
    val sampleSource = NewsSource(
        id = "sample-news",
        name = "Sample News Channel",
        description = "Your most trusted source for sample news and events happening in the preview.",
        url = "https://example.com/sample",
        category = "general",
        language = "en",
        country = "us"
    )
    MaterialTheme {
        SourceListItem(
            itemState = SourceItemUiState(source = sampleSource, isSelected = true),
            onCheckedChange = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Source List Item - Not Selected")
@Composable
fun SourceListItemNotSelectedPreview() {
    val sampleSource = NewsSource(
        id = "another-sample",
        name = "Another Daily Sample",
        description = "Breaking samples every hour. This description is a bit longer to test how it handles overflow with multiple lines and such.",
        url = "https.example.com/another",
        category = "technology",
        language = "en",
        country = "us"
    )
    MaterialTheme {
        SourceListItem(
            itemState = SourceItemUiState(source = sampleSource, isSelected = false),
            onCheckedChange = { _, _ -> }
        )
    }
}
