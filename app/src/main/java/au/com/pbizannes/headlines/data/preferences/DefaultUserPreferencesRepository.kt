package au.com.pbizannes.headlines.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import au.com.pbizannes.headlines.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class DefaultUserPreferencesRepository @Inject constructor(
    private val context: Context // Or @ApplicationContext context: Context if using Hilt
): UserPreferencesRepository {
    override fun selectedSourceIdsFlow(): Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                // You can log the error or handle it as needed
                emit(emptyPreferences()) // Emit empty preferences if an error occurs
            } else {
                throw exception // Rethrow other exceptions
            }
        }
        .map { preferences ->
            // Get the set of selected source IDs, defaulting to an empty set if not found
            preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] ?: emptySet()
        }

    override suspend fun updateSelectedSourceIds(sourceIds: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] = sourceIds
        }
    }

    override suspend fun addSourceId(sourceId: String) {
        context.dataStore.edit { preferences ->
            val currentIds = preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] ?: emptySet()
            preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] = currentIds + sourceId
        }
    }

    override suspend fun removeSourceId(sourceId: String) {
        context.dataStore.edit { preferences ->
            val currentIds = preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] ?: emptySet()
            preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] = currentIds - sourceId
        }
    }

    override suspend fun clearSelectedSourceIds() {
        context.dataStore.edit { preferences ->
            preferences.remove(UserPreferencesKeys.SELECTED_SOURCE_IDS)
            // Or to set to an empty set:
            // preferences[UserPreferencesKeys.SELECTED_SOURCE_IDS] = emptySet()
        }
    }
}
