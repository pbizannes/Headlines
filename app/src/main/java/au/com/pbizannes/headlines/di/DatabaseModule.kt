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
@InstallIn(SingletonComponent::class) // Provides instances that live as long as the application
object DatabaseModule {

    @Provides
    @Singleton // Ensures only one instance of the database is created
    fun provideHeadlinesDatabase(@ApplicationContext context: Context): HeadlinesDatabase {
        return HeadlinesDatabase.getDatabase(context)
    }

    @Provides
    // No @Singleton needed here if ArticleDao is stateless,
    // Hilt will provide the same instance when requested from the same Database instance.
    fun provideArticleDao(database: HeadlinesDatabase): ArticleDao {
        return database.articleDao()
    }
}
