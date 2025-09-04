package au.com.pbizannes.headlines.domain.mapper

import au.com.pbizannes.headlines.data.models.ArticleData
import au.com.pbizannes.headlines.data.models.ArticleSourceData
import au.com.pbizannes.headlines.data.models.NewsSourceData
import au.com.pbizannes.headlines.domain.models.Article
import au.com.pbizannes.headlines.domain.models.ArticleSource
import au.com.pbizannes.headlines.domain.models.NewsSource
import au.com.pbizannes.headlines.util.Tools

fun List<ArticleData>.toDomainArticleList(): List<Article> = DomainMapper.toDomainArticleList(this)
fun ArticleData.toDomainArticle(): Article = DomainMapper.toDomainArticle(this)
fun List<Article>.fromDomainArticleList(): List<ArticleData> = DomainMapper.fromDomainArticleList(this)
fun Article.fromDomainArticle(): ArticleData = DomainMapper.fromDomainArticle(this)
fun List<NewsSourceData>.toDomainNewsSourceList(): List<NewsSource> = DomainMapper.toDomainNewsSourceList(this)
fun NewsSourceData.toDomainNewsSource(): NewsSource = DomainMapper.toDomainNewsSource(this)


object DomainMapper {
    fun toDomainArticleList (articleEntities: List<ArticleData>): List<Article> {
        return articleEntities.map { it.toDomainArticle() }
    }

    fun toDomainArticle(ArticleData: ArticleData): Article {
        return Article(
            url = ArticleData.url,
            source = ArticleSource(ArticleData.source.id, ArticleData.source.name),
            author = ArticleData.author,
            title = ArticleData.title,
            description = ArticleData.description,
            urlToImage = ArticleData.urlToImage,
            publishedAt = Tools.then(ArticleData.publishedAt),
            content = ArticleData.content
        )
    }

    fun toDomainNewsSourceList(newSourceData:List<NewsSourceData>): List<NewsSource> {
        return newSourceData.map { it.toDomainNewsSource()   }
    }
    fun toDomainNewsSource(newsSourceData: NewsSourceData): NewsSource {
        return NewsSource(
            id = newsSourceData.id,
            name = newsSourceData.name,
            category = newsSourceData.category,
            language = newsSourceData.language,
            url = newsSourceData.url,
            country = newsSourceData.country,
            description = newsSourceData.description,
        )
    }

    fun fromDomainArticleList(articles: List<Article>): List<ArticleData> {
        return articles.map { it.fromDomainArticle() }
    }

    fun fromDomainArticle(article: Article): ArticleData {
        return ArticleData(
            url = article.url,
            source = ArticleSourceData(article.source.id, article.source.name),
            author = article.author,
            title = article.title,
            description = article.description,
            urlToImage = article.urlToImage,
            publishedAt = article.publishedAt.toString(),
            content = article.content
        )
    }
}
