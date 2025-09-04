package au.com.pbizannes.headlines.di

import android.content.Context
import au.com.pbizannes.headlines.data.source.local.ArticleDao
import au.com.pbizannes.headlines.data.source.local.HeadlinesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHeadlinesDatabase(@ApplicationContext context: Context): HeadlinesDatabase {
        return HeadlinesDatabase.getDatabase(context)
    }

    @Provides
    fun provideArticleDao(database: HeadlinesDatabase): ArticleDao {
        return database.articleDao()
    }
}
