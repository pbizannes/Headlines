package au.com.pbizannes.headlines.presentation.headlines

import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.pbizannes.headlines.data.models.ArticleData
import au.com.pbizannes.headlines.data.models.ArticleSourceData
import au.com.pbizannes.headlines.domain.models.Article
import au.com.pbizannes.headlines.domain.models.ArticleSource
import au.com.pbizannes.headlines.domain.repository.ArticleRepository
import au.com.pbizannes.headlines.domain.repository.NewsRepository
import au.com.pbizannes.headlines.domain.repository.UserPreferencesRepository
import au.com.pbizannes.headlines.presentation.HeadlinesUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    private val newsRepository: NewsRepository,
    private val articleRepository: ArticleRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _headlinesUIState = MutableStateFlow<HeadlinesUIState>(HeadlinesUIState.Loading)
    val headlinesUIState: StateFlow<HeadlinesUIState> = _headlinesUIState.asStateFlow()

    fun isArticleBookmarked(articleUrl: String): Flow<Boolean> {
        return articleRepository.isArticleSaved(articleUrl)
    }

    fun onBookmarkCheckedChange(article: Article, isBookmarked: Boolean) {
        viewModelScope.launch {
            if (isBookmarked) {
                articleRepository.saveArticle(article)
            } else {
                val existingArticle = articleRepository.getArticleByUrl(article.url)
                existingArticle?.let {
                    articleRepository.deleteArticle(it)
                }
            }
        }
    }

    fun loadContent() {
        viewModelScope.launch {
            _headlinesUIState.value = HeadlinesUIState.Loading

            try {
                userPreferencesRepository.selectedSourceIdsFlow().firstOrNull().let { selectedIds ->
                    val articleSources = selectedIds?.map { id ->
                        ArticleSource(
                            id = id,
                            name = id
                        )
                    } ?: listOf()

                    val result = newsRepository.getHeadlines(articleSources)
                    when {
                        result.isFailure -> {
                            HeadlinesUIState.Error("Error fetching headlines: ${result.exceptionOrNull()?.message ?: "Unknown error"}")
                        }
                        else -> {
                            result.getOrNull()?.let { articles ->
                                _headlinesUIState.value = HeadlinesUIState.Success(articles)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _headlinesUIState.value =
                    HeadlinesUIState.Error("Failed to load headlines: ${e.message ?: "Unknown error"}")
            }
        }
    }
}