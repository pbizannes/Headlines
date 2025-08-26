package au.com.pbizannes.headlines.presentation.saved

import au.com.pbizannes.headlines.domain.model.Article

sealed interface SavedArticlesUiState {
    object Loading : SavedArticlesUiState
    data class Success(val articles: List<Article>) : SavedArticlesUiState
    data class Error(val message: String) : SavedArticlesUiState // Though less common for local DB reads
    object Empty : SavedArticlesUiState // Specific state for when no articles are saved
}
