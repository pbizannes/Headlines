package au.com.pbizannes.headlines.domain.models

import java.time.Instant

data class Article(
    val url: String,
    val source: ArticleSource,
    val author: String?, // Author can sometimes be null
    val title: String,
    val description: String?, // Description can sometimes be null
    val urlToImage: String?, // URL to image can sometimes be null
    val publishedAt: Instant,
    val content: String? // Content can sometimes be null
)
