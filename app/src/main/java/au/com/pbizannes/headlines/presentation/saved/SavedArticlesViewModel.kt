package au.com.pbizannes.headlines.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.pbizannes.headlines.domain.models.Article
import au.com.pbizannes.headlines.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedArticlesViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    val uiState: StateFlow<SavedArticlesUiState> =
        articleRepository.getAllBookmarkedArticles()
            .map { articles ->
                if (articles.isEmpty()) {
                    SavedArticlesUiState.Empty
                } else {
                    SavedArticlesUiState.Success(articles)
                }
            }
            .onStart { emit(SavedArticlesUiState.Loading) }
            .catch { throwable ->
                emit(SavedArticlesUiState.Error(throwable.message ?: "Failed to load saved articles"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SavedArticlesUiState.Loading
            )

    fun deleteSavedArticle(article: Article) {
        viewModelScope.launch {
            articleRepository.deleteArticle(article)
        }
    }

    fun deleteAllSavedArticles() {
        viewModelScope.launch {
            articleRepository.deleteAllArticles()
        }
    }
}
