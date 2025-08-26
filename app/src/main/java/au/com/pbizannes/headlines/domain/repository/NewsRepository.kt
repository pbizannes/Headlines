package au.com.pbizannes.headlines.domain.repository

import au.com.pbizannes.headlines.domain.model.Article
import au.com.pbizannes.headlines.domain.model.ArticleSource
import au.com.pbizannes.headlines.domain.model.NewsSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository for news, news sources, selected sources and selected news.
 */
interface NewsRepository {
    /**
     * Get a List of [List<NewsSource>].
     */
    suspend fun getSources(): Result<List<NewsSource>>

    suspend fun getHeadlines(sources: List<ArticleSource>): Flow<Article>
}