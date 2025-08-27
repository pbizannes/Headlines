package au.com.pbizannes.headlines.presentation.headlines

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import au.com.pbizannes.headlines.data.preferences.UserPreferencesRepository
import au.com.pbizannes.headlines.domain.model.Article
import au.com.pbizannes.headlines.domain.model.ArticleSource
import au.com.pbizannes.headlines.domain.repository.ArticleRepository
import au.com.pbizannes.headlines.domain.repository.NewsRepository
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

    private val collectedArticles = MutableStateFlow<List<Article>>(emptyList())

    // Function to check if an article is bookmarked/saved
    // This will be called by each ArticleItem to determine initial checkbox state
    fun isArticleBookmarked(articleUrl: String): Flow<Boolean> {
        return articleRepository.isArticleSaved(articleUrl)
    }

    // Function to handle checkbox state change
    fun onBookmarkCheckedChange(article: Article, isBookmarked: Boolean) {
        viewModelScope.launch {
            if (isBookmarked) {
                articleRepository.saveArticle(article)
            } else {
                // Ensure you have the full article details if deleting by object,
                // or just use the URL if your repository supports deleteByUrl.
                // Fetching the article again if only URL is available might be needed
                // if deleteArticle requires the full object and it wasn't passed directly.
                val existingArticle = articleRepository.getArticleByUrl(article.url)
                existingArticle?.let {
                    articleRepository.deleteArticle(it)
                }
                // Or if your deleteArticle can handle a potentially partial Article object
                // (e.g. if the primary key 'url' is enough for Room to find and delete)
                // articleBookmarkRepository.deleteArticle(article)
            }
        }
    }

    fun loadContent() {
        viewModelScope.launch {
            _headlinesUIState.value = HeadlinesUIState.Loading
            collectedArticles.value = emptyList() // Reset articles

            // 1. Get selected source IDs from DataStore
            userPreferencesRepository.selectedSourceIdsFlow.firstOrNull().let { selectedIds ->
                // 4. Fetch headlines for the filtered sources
                // This part assumes getHeadlines emits articles one by one or in chunks
                // and we need to collect them.
                try {
                    val articleSources = selectedIds?.map { id ->
                        ArticleSource(
                            id = id,
                            name = id
                        ) } ?: listOf()
                    val selectedNewsSources =
                        newsRepository.getHeadlines(articleSources)
                            .catch { e -> // Catch errors from the getHeadlines Flow itself
                                _headlinesUIState.value =
                                    HeadlinesUIState.Error("Error fetching headlines: ${e.message ?: "Unknown error"}")
                            }
                            .collect { article ->
                                // Add to our collected list and update UI
                                // This is a common pattern if articles stream in.
                                // If getHeadlines() was meant to emit a single List<Article>,
                                // the .collect logic would be simpler.
                                val currentArticles = collectedArticles.value.toMutableList()
                                currentArticles.add(article)
                                collectedArticles.value = currentArticles
                                // Update UI state with the incrementally built list
                                // Debounce or update less frequently if articles come very fast
                                _headlinesUIState.value = HeadlinesUIState.Success(collectedArticles.value)
                            }

                    // If, after collecting all articles, the list is still empty,
                    // but sources were available, it implies no articles for those sources.
                    if (collectedArticles.value.isEmpty() && _headlinesUIState.value !is HeadlinesUIState.Error) {
                        _headlinesUIState.value = HeadlinesUIState.Success(emptyList())
                    }

                } catch (e: Exception) { // Catch any other exceptions during the process
                    _headlinesUIState.value =
                        HeadlinesUIState.Error("Failed to load headlines: ${e.message ?: "Unknown error"}")
                }
            }
        }
    }
}