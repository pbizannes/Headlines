package au.com.pbizannes.headlines.presentation

import au.com.pbizannes.headlines.domain.models.Article

sealed interface HeadlinesUIState {
    object Loading : HeadlinesUIState
    data class Success(val articles: List<Article>) : HeadlinesUIState
    data class Error(val message: String) : HeadlinesUIState
}
