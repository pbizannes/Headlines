package au.com.pbizannes.headlines.data

import au.com.pbizannes.headlines.data.source.local.ArticleDao
import au.com.pbizannes.headlines.domain.model.Article
import au.com.pbizannes.headlines.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DefaultArticleRepository @Inject constructor(
    private val articleDao: ArticleDao,
    // ... other dependencies like your NewsService
): ArticleRepository {
    override fun getAllBookmarkedArticles(): Flow<List<Article>> {
        return articleDao.getAllArticles()
    }

    override suspend fun saveArticle(article: Article) {
        articleDao.insertArticle(article)
    }

    override suspend fun deleteArticle(article: Article) {
        articleDao.deleteArticle(article)
    }

    override suspend fun deleteAllArticles() {
        articleDao.deleteAllArticles()
    }

    override fun isArticleSaved(url: String): Flow<Boolean> {
        return articleDao.isArticleBookmarked(url)
    }

    override suspend fun getArticleByUrl(url: String): Article? {
        return articleDao.getArticleByUrl(url)
    }
}
