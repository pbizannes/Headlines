package au.com.pbizannes.headlines.di

import au.com.pbizannes.headlines.data.DefaultArticleRepository
import au.com.pbizannes.headlines.data.DefaultNewsRepository
import au.com.pbizannes.headlines.data.source.local.ArticleDao
import au.com.pbizannes.headlines.data.source.network.NewsService
import au.com.pbizannes.headlines.domain.repository.ArticleRepository
import au.com.pbizannes.headlines.domain.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideInvoiceRepository(newsService: NewsService): NewsRepository =
        DefaultNewsRepository(newsService = newsService)

    @Provides
    @Singleton
    fun provideArticleRepository(articleDao: ArticleDao): ArticleRepository =
        DefaultArticleRepository(articleDao = articleDao)
}