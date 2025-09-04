package au.com.pbizannes.headlines.domain.repository

import au.com.pbizannes.headlines.domain.models.Article
import au.com.pbizannes.headlines.data.models.ArticleSourceData
import au.com.pbizannes.headlines.data.models.NewsSourceData
import au.com.pbizannes.headlines.domain.models.ArticleSource
import au.com.pbizannes.headlines.domain.models.NewsSource

/**
 * Repository for news, news sources, selected sources and selected news.
 */
interface NewsRepository {
    /**
     * Get a List of [List<NewsSource>].
     */
    suspend fun getSources(): Result<List<NewsSource>>

    suspend fun getHeadlines(sources: List<ArticleSource>): Result<List<Article>>
}