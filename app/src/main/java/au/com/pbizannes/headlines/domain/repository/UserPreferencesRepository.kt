package au.com.pbizannes.headlines.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    fun selectedSourceIdsFlow(): Flow<Set<String>>

    suspend fun updateSelectedSourceIds(sourceIds: Set<String>)

    suspend fun addSourceId(sourceId: String)

    suspend fun removeSourceId(sourceId: String)

    suspend fun clearSelectedSourceIds()
}
