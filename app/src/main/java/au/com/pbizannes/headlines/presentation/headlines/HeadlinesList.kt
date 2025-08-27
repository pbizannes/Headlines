package au.com.pbizannes.headlines.presentation.headlines

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import au.com.pbizannes.headlines.domain.model.Article
import au.com.pbizannes.headlines.domain.model.ArticleSource
import au.com.pbizannes.headlines.presentation.components.ArticleComponent
import au.com.pbizannes.headlines.ui.theme.HeadlinesTheme

@Composable
fun HeadlinesList(
    articles: List<Article>,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HeadlinesViewModel = hiltViewModel()
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp) // Add some padding around the list
    ) {
        // The 'items' extension function is the simplest way to display a list
        items(
            items = articles,
            key = { article -> article.url } // Provide a stable key for each item
        ) { article ->
            ArticleComponent(
                article = article,
                isBookmarkedFlow = viewModel.isArticleBookmarked(article.url),
                onArticleClick = onArticleClick,
                onBookmarkToggle = { currentArticle, isNowBookmarked ->
                    viewModel.onBookmarkCheckedChange(currentArticle, isNowBookmarked)
                },
                modifier = Modifier.testTag("headlines_list_item")
            )
        }
    }
}

@Preview(showBackground = true, name = "Headlines List Preview")
@Composable
fun HeadlinesListPreview() {
    // Create some sample Article data for the preview
    val sampleArticles = listOf(
        Article(
            source = ArticleSource(id = "bbc-news", name = "BBC News"),
            author = "BBC News Author",
            title = "Major Event Happens: A Long Title That Might Wrap to Multiple Lines",
            description = "This is a detailed description of the major event that has occurred, providing context and implications for everyone involved.",
            url = "https://www.bbc.com/news/article1",
            urlToImage = "https://via.placeholder.com/600x400.png?text=BBC+News+Image", // Placeholder image
            publishedAt = "2023-10-27T10:00:00Z",
            content = "Full content of the article..."
        ),
        Article(
            source = ArticleSource(id = "tech-crunch", name = "TechCrunch"),
            author = "Tech Writer",
            title = "New Gadget Released: Innovative Features Unveiled",
            description = "A new groundbreaking gadget has been released today, promising to revolutionize the way we interact with technology. It has many cool features.",
            url = "https://techcrunch.com/article2",
            urlToImage = "https://via.placeholder.com/600x400.png?text=TechCrunch+Image", // Placeholder image
            publishedAt = "2023-10-27T09:30:00Z",
            content = "More details about the gadget..."
        ),
        Article(
            source = ArticleSource(id = null, name = "Local Blog"),
            author = "Jane Doe",
            title = "Community Update: What's Happening Locally This Week",
            description = "An update on local events and news from our community. Support local initiatives and stay informed.",
            url = "https://www.localblog.com/news/update1",
            urlToImage = null, // Example with no image
            publishedAt = "2023-10-26T15:00:00Z",
            content = "Further content..."
        ),
        Article(
            source = ArticleSource(id = "the-verge", name = "The Verge"),
            author = "The Verge Staff",
            title = "Science Discovery: Exploring the Cosmos",
            description = "Scientists have made a significant discovery about the universe, opening new avenues for research and understanding.",
            url = "https://www.theverge.com/science/discover",
            urlToImage = "https://via.placeholder.com/600x400.png?text=The+Verge+Image", // Placeholder image
            publishedAt = "2023-10-25T12:00:00Z",
            content = "Deep dive into the science..."
        )
    )

    HeadlinesTheme {
        HeadlinesList(
            articles = sampleArticles,
            onArticleClick = { article ->
                // In a preview, you can log or do nothing on click
                println("Preview: Clicked on article - ${article.title}")
            }
        )
    }
}

@Preview(showBackground = true, name = "Empty Headlines List Preview")
@Composable
fun EmptyHeadlinesListPreview() {
    HeadlinesTheme {
        HeadlinesList(
            articles = emptyList(), // Pass an empty list
            onArticleClick = { article ->
                println("Preview: Clicked on article - ${article.title}")
            }
        )
    }
}

