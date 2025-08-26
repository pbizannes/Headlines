package au.com.pbizannes.headlines.presentation.saved

import android.content.Context
import androidx.activity.result.launch
import androidx.compose.ui.geometry.isEmpty
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import au.com.pbizannes.headlines.domain.model.Article
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

// UI State for Saved Articles Screen

@HiltViewModel
class SavedArticlesViewModel @Inject constructor(
    private val articleRepository: ArticleRepository // Your repository for saved articles
) : ViewModel() {

    val uiState: StateFlow<SavedArticlesUiState> =
        articleRepository.getAllBookmarkedArticles() // Assuming this returns Flow<List<Article>>
            .map<List<Article>, SavedArticlesUiState> { articles ->
                if (articles.isEmpty()) {
                    SavedArticlesUiState.Empty
                } else {
                    SavedArticlesUiState.Success(articles)
                }
            }
            .onStart { emit(SavedArticlesUiState.Loading) }
            .catch { throwable ->
                // Errors are less common when reading from a local DB,
                // but good to have for completeness.
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
            // The UI will automatically update because uiState observes the Flow from the repository
        }
    }

    fun deleteAllSavedArticles() {
        viewModelScope.launch {
            articleRepository.deleteAllArticles()
        }
    }
}
