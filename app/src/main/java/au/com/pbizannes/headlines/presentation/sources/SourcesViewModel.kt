package au.com.pbizannes.headlines.presentation.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.pbizannes.headlines.domain.models.NewsSource
import au.com.pbizannes.headlines.domain.repository.NewsRepository
import au.com.pbizannes.headlines.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SourceItemUiState(
    val source: NewsSource,
    val isSelected: Boolean
)

sealed interface SourcesScreenUiState {
    object Loading : SourcesScreenUiState
    data class Success(val sourceItems: List<SourceItemUiState>) : SourcesScreenUiState
    data class Error(val message: String) : SourcesScreenUiState
}

@HiltViewModel
class SourcesViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<SourcesScreenUiState> =
        combine(
            flow { emit(newsRepository.getSources()) },
            userPreferencesRepository.selectedSourceIdsFlow()
        ) { sourcesResult, selectedIds ->
            sourcesResult.fold(
                onSuccess = { allSources ->
                    val sourceItemList = allSources.map { source ->
                        SourceItemUiState(
                            source = source,
                            isSelected = source.id != null && selectedIds.contains(source.id)
                        )
                    }
                    if (sourceItemList.isEmpty()) {
                        SourcesScreenUiState.Success(emptyList())
                    } else {
                        SourcesScreenUiState.Success(sourceItemList)
                    }
                },
                onFailure = { exception ->
                    SourcesScreenUiState.Error(exception.message ?: "Failed to load sources")
                }
            )
        }
            .onStart { emit(SourcesScreenUiState.Loading) }
            .catch {e ->
                emit(SourcesScreenUiState.Error(e.message ?: "An unexpected error occurred"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SourcesScreenUiState.Loading
            )

    fun onSourceSelectedChange(sourceId: String, isSelected: Boolean) {
        viewModelScope.launch {
            if (isSelected) {
                userPreferencesRepository.addSourceId(sourceId)
            } else {
                userPreferencesRepository.removeSourceId(sourceId)
            }
        }
    }

    fun selectAllSources() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is SourcesScreenUiState.Success) {
                val allSourceIds = currentState.sourceItems.mapNotNull { it.source.id }
                userPreferencesRepository.updateSelectedSourceIds(allSourceIds.toSet())
            }
        }
    }

    fun deselectAllSources() {
        viewModelScope.launch {
            userPreferencesRepository.clearSelectedSourceIds()
        }
    }
}
