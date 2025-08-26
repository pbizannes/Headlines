package au.com.pbizannes.headlines.presentation.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import au.com.pbizannes.headlines.domain.model.Article
import au.com.pbizannes.headlines.domain.model.ArticleSource
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun SavedArticleItem(
    article: Article,
    onArticleClick: (Article) -> Unit,
    onDeleteClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { onArticleClick(article) }
                .padding(12.dp)
        ) {
            article.urlToImage?.let { imageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f) // Title takes available space
                )
                IconButton(
                    onClick = { onDeleteClick(article) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Saved Article",
                        tint = MaterialTheme.colorScheme.error // Use error color for delete
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            article.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.source.name, // Assuming name is not nullable in your final model
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = article.publishedAt, // Consider formatting
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}


@Preview(showBackground = true, name = "Saved Article Item Preview")
@Composable
fun SavedArticleItemPreview() {
    val sampleArticle = Article(
        source = ArticleSource(id = "sample-source", name = "Sample News Weekly"),
        author = "Jane Previewer",
        title = "A Saved Article: Exploring Local Storage Marvels",
        description = "This is a preview of how a saved article item will look within the application. It includes a delete button.",
        url = "https://example.com/saved-article",
        urlToImage = "https://via.placeholder.com/600x400.png?text=Saved+Article",
        publishedAt = "2023-03-15T10:30:00Z",
        content = "Full content of the saved article goes here..."
    )
    MaterialTheme {
        SavedArticleItem(
            article = sampleArticle,
            onArticleClick = {},
            onDeleteClick = {}
        )
    }
}