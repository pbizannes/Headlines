package au.com.pbizannes.headlines.presentation.saved

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import au.com.pbizannes.headlines.domain.model.Article

@Composable
fun SavedArticlesList(
    articles: List<Article>,
    onArticleClick: (Article) -> Unit,
    onDeleteClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = articles,
            key = { article -> article.url } // Use a stable key
        ) { article ->
            SavedArticleItem(
                article = article,
                onArticleClick = onArticleClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}
