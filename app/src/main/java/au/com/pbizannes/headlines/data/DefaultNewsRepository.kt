package au.com.pbizannes.headlines.data

import au.com.pbizannes.headlines.BuildConfig
import au.com.pbizannes.headlines.data.source.network.NewsService
import au.com.pbizannes.headlines.domain.mapper.toDomainArticleList
import au.com.pbizannes.headlines.domain.mapper.toDomainNewsSourceList
import au.com.pbizannes.headlines.domain.models.Article
import au.com.pbizannes.headlines.domain.models.ArticleSource
import au.com.pbizannes.headlines.domain.models.NewsSource
import au.com.pbizannes.headlines.domain.repository.NewsRepository

class DefaultNewsRepository(val newsService: NewsService) : NewsRepository {
    val apiKey = BuildConfig.API_KEY
    override suspend fun getSources(): Result<List<NewsSource>> {
        return try {
            val response = newsService.getSources(apiKey)

            val responseBody = response.body()
            if (response.isSuccessful) {
                if (responseBody != null) {
                    Result.success(responseBody.sources.toDomainNewsSourceList())
                } else {
                    Result.failure(NoSuchElementException())
                }
            } else {
                Result.failure(NoSuchElementException())
            }
        } catch (error: Exception) {
            Result.failure(NoSuchElementException())
        }
    }

    override suspend fun getHeadlines(sources: List<ArticleSource>): Result<List<Article>> {
        val country = if (sources.isEmpty()) "us" else null

        return try {
            val response = newsService.getHeadlines(
                apiKey = apiKey,
                country = country,
                sourceList = sources.map { it.id }.requireNoNulls()
            )

            val responseBody = response.body()
            if (response.isSuccessful) {
                if (responseBody != null) {
                    Result.success(responseBody.articleEntities.toDomainArticleList())
                } else {
                    Result.failure(NoSuchElementException())
                }
            } else {
                Result.failure(NoSuchElementException())
            }
        } catch (error: Exception) {
            Result.failure(NoSuchElementException())
        }
    }
}