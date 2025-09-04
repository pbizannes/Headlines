package au.com.pbizannes.headlines.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import au.com.pbizannes.headlines.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DefaultUserPreferencesRepository (
    private val dataStore: DataStore<Preferences>
): UserPreferencesRepository {
    override fun selectedSourceIdsFlow(): Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] ?: emptySet()
        }

    override suspend fun updateSelectedSourceIds(sourceIds: Set<String>) {
        dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] = sourceIds
        }
    }

    override suspend fun addSourceId(sourceId: String) {
        dataStore.edit { preferences ->
            val currentIds = preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] ?: emptySet()
            preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] = currentIds + sourceId
        }
    }

    override suspend fun removeSourceId(sourceId: String) {
        dataStore.edit { preferences ->
            val currentIds = preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] ?: emptySet()
            preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] = currentIds - sourceId
        }
    }

    override suspend fun clearSelectedSourceIds() {
        dataStore.edit { preferences ->
            preferences.remove(UserPreferencesKeys.SELECTED_SOURCE_IDS)
        }
    }
}
