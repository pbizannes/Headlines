package au.com.pbizannes.headlines.data

import au.com.pbizannes.headlines.data.source.local.ArticleDao
import au.com.pbizannes.headlines.domain.mapper.fromDomainArticle
import au.com.pbizannes.headlines.domain.mapper.toDomainArticle
import au.com.pbizannes.headlines.domain.mapper.toDomainArticleList
import au.com.pbizannes.headlines.domain.models.Article
import au.com.pbizannes.headlines.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultArticleRepository @Inject constructor(
    private val articleDao: ArticleDao,
): ArticleRepository {
    override fun getAllBookmarkedArticles(): Flow<List<Article>> {
        return articleDao.getAllArticles().map { it.toDomainArticleList() }
    }

    override suspend fun saveArticle(article: Article) {
        articleDao.insertArticle(article.fromDomainArticle())
    }

    override suspend fun deleteArticle(article: Article) {
        articleDao.deleteArticle(article.fromDomainArticle())
    }

    override suspend fun deleteAllArticles() {
        articleDao.deleteAllArticles()
    }

    override fun isArticleSaved(url: String): Flow<Boolean> {
        return articleDao.isArticleBookmarked(url)
    }

    override suspend fun getArticleByUrl(url: String): Article? {
        return articleDao.getArticleByUrl(url)?.toDomainArticle()
    }
}
