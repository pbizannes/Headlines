package au.com.pbizannes.headlines.domain.repository

import au.com.pbizannes.headlines.domain.models.Article
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {
    fun getAllBookmarkedArticles(): Flow<List<Article>>

    suspend fun saveArticle(article: Article)

    suspend fun deleteArticle(article: Article)

    suspend fun deleteAllArticles()

    fun isArticleSaved(url: String): Flow<Boolean>

    suspend fun getArticleByUrl(url: String): Article?
}
