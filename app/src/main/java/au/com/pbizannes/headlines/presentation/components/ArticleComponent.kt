package au.com.pbizannes.headlines.presentation.components

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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import au.com.pbizannes.headlines.domain.models.Article
import au.com.pbizannes.headlines.domain.models.ArticleSource
import au.com.pbizannes.headlines.presentation.mapper.ArticleMapper
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Composable
fun ArticleComponent(
    article: Article,
    isBookmarkedFlow: Flow<Boolean>,
    onArticleClick: (Article) -> Unit,
    onBookmarkToggle: (Article, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBookmarked by isBookmarkedFlow.collectAsStateWithLifecycle(initialValue = false)

    val displayableArticle = ArticleMapper.toPresentation(article)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable { onArticleClick(article) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
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
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onBookmarkToggle(article, !isBookmarked) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove from Bookmarks" else "Add to Bookmarks",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                    text = displayableArticle.source,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = displayableArticle.publishedAtFormatted,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Article Item - Not Bookmarked")
@Composable
fun ArticleComponentNotBookmarkedPreview() {
    val sampleArticleData = Article(
        source = ArticleSource(id = "sample", name = "Sample News"),
        author = "Author Name",
        title = "Sample Article Title - Not Bookmarked",
        description = "This is a sample description for the article to see how it looks.",
        url = "https://example.com/sample",
        urlToImage = "https://via.placeholder.com/600x400.png?text=Article+Image",
        publishedAt = Instant.parse("2023-01-02T14:00:00Z"),
        content = "Sample content..."
    )
    MaterialTheme {
        ArticleComponent(
            article = sampleArticleData,
            isBookmarkedFlow = kotlinx.coroutines.flow.flowOf(false),
            onArticleClick = {},
            onBookmarkToggle = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Article Item - Bookmarked")
@Composable
fun ArticleComponentBookmarkedPreview() {
    val sampleArticleData = Article(
        source = ArticleSource(id = "sample", name = "Sample News"),
        author = "Author Name",
        title = "Sample Article Title - Bookmarked and a bit longer",
        description = "This article is bookmarked. The description can be a bit longer to test text overflow and layout constraints properly.",
        url = "https://example.com/sample2",
        urlToImage = "https://via.placeholder.com/600x400.png?text=Bookmarked",
        publishedAt = Instant.parse("2023-01-02T14:00:00Z"),
        content = "Sample content..."
    )
    MaterialTheme {
        ArticleComponent(
            article = sampleArticleData,
            isBookmarkedFlow = kotlinx.coroutines.flow.flowOf(true), // Simulate bookmarked
            onArticleClick = {},
            onBookmarkToggle = { _, _ -> }
        )
    }
}
