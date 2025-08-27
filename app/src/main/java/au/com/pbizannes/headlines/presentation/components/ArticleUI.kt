package au.com.pbizannes.headlines.presentation.components

data class ArticleUI(
    val url: String,
    val source: String,
    val author: String?,
    val title: String,
    val description: String?,
    val urlToImage: String?,
    val publishedAtFormatted: String,
    val content: String?,
)