package au.com.pbizannes.headlines.presentation.saved

import au.com.pbizannes.headlines.domain.models.Article

sealed interface SavedArticlesUiState {
    object Loading : SavedArticlesUiState
    data class Success(val articles: List<Article>) : SavedArticlesUiState
    data class Error(val message: String) : SavedArticlesUiState
    object Empty : SavedArticlesUiState
}
