package au.com.pbizannes.headlines.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
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
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_preferences") }
        )
    }

    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
         dataStore: DataStore<Preferences>,
    ): UserPreferencesRepository {
        return DefaultUserPreferencesRepository(dataStore)
    }
}