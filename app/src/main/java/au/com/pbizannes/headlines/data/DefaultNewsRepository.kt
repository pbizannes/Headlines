package au.com.pbizannes.headlines.data

import au.com.pbizannes.headlines.BuildConfig
import au.com.pbizannes.headlines.data.source.network.NewsService
import au.com.pbizannes.headlines.domain.model.Article
import au.com.pbizannes.headlines.domain.model.ArticleSource
import au.com.pbizannes.headlines.domain.model.NewsSource
import au.com.pbizannes.headlines.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

class DefaultNewsRepository(val newsService: NewsService) : NewsRepository {
    val apiKey = BuildConfig.API_KEY
    override suspend fun getSources(): Result<List<NewsSource>> {
        return try {
            val response = newsService.getSources(apiKey)

            val responseBody = response.body()
            if (response.isSuccessful) {
                if (responseBody != null) {
                    Result.success(responseBody.sources)
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

    override suspend fun getHeadlines(sources: List<ArticleSource>): Flow<Article> {
        return try {
            val country = if (sources.isEmpty()) "us" else null
            val response = newsService.getHeadlines(
                apiKey = apiKey,
                country = country,
                sourceList = sources.map { it.id }.requireNoNulls()
            )

            val responseBody = response.body()
            if (response.isSuccessful) {
                responseBody?.articles?.asFlow() ?: throw NoSuchElementException()
            } else {
                throw NoSuchElementException()
            }
        } catch (error: Exception) {
            throw error
        }
    }
}