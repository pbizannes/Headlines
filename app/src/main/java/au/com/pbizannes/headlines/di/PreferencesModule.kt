package au.com.pbizannes.headlines.di

import android.content.Context
import au.com.pbizannes.headlines.data.preferences.DefaultUserPreferencesRepository
import au.com.pbizannes.headlines.domain.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        @ApplicationContext context: Context
    ): UserPreferencesRepository {
        return DefaultUserPreferencesRepository(context)
    }
}