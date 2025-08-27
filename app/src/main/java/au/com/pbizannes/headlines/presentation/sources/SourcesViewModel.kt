package au.com.pbizannes.headlines.presentation.sources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.pbizannes.headlines.domain.model.NewsSource
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

// UI State for Sources Screen
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

    // Combine the flow of all sources and the flow of selected source IDs
    val uiState: StateFlow<SourcesScreenUiState> =
        combine(
            // Flow for all available sources (assuming this is a one-shot fetch converted to Flow or
            // the repository itself returns a Flow<Result<List<NewsSource>>>)
            // For simplicity, let's assume getSources() is suspend and we convert its result to a flow here.
            flow { emit(newsRepository.getSources()) }, // newsRepository.getSources() returns Result<List<NewsSource>>
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
                        // If allSources was empty initially
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
            .catch {e -> // Catch errors from the combine or downstream
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
            // The uiState will automatically update because selectedSourceIdsFlow will emit a new set,
            // triggering the 'combine' operator to re-evaluate.
        }
    }

    // Optional: Functions to select all or none
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
