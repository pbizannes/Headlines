package au.com.pbizannes.headlines.presentation.saved

import app.cash.turbine.test
import au.com.pbizannes.headlines.data.models.ArticleData
import au.com.pbizannes.headlines.data.models.ArticleSourceData
import au.com.pbizannes.headlines.domain.repository.ArticleRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.Instant

@ExperimentalCoroutinesApi
class SavedArticlesViewModelTest {

    private lateinit var mockArticleRepository: ArticleRepository
    private lateinit var viewModel: SavedArticlesViewModel

    private val articleSource = ArticleSourceData(id = "test-src", name = "Test Source")
    private val savedArticleData1 = ArticleData("url1", articleSource, "Author 1", "Saved Title 1", "Desc 1", "img1",
        Instant.now().toString(), "Content1")
    private val savedArticleData2 = ArticleData("url2", articleSource, "Author 2", "Saved Title 2", "Desc 2", "img2", Instant.now().minusSeconds(300).toString(), "Content2")

    @Before
    fun setUp() {
        mockArticleRepository = mockk()

        every { mockArticleRepository.getAllBookmarkedArticles() } returns flowOf(emptyList()) // Default to no saved articles
        coJustRun { mockArticleRepository.deleteArticle(any()) }
        coJustRun { mockArticleRepository.deleteAllArticles() }

        viewModel = SavedArticlesViewModel(mockArticleRepository)
    }

    @Test
    fun `uiState emits Loading then Empty when no saved articles`() = runTest {
        viewModel.uiState.test {
            Assert.assertEquals("Initial state should be Loading", SavedArticlesUiState.Loading, awaitItem())
            Assert.assertEquals("State should be Empty if repo returns empty list", SavedArticlesUiState.Empty, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        verify { mockArticleRepository.getAllBookmarkedArticles() }
    }

    @Test
    fun `uiState emits Loading then Success with articles when repository returns saved articles`() = runTest {
        val savedArticles = listOf(savedArticleData1, savedArticleData2)
        every { mockArticleRepository.getAllBookmarkedArticles() } returns flowOf(savedArticles)
        viewModel = SavedArticlesViewModel(mockArticleRepository)

        viewModel.uiState.test {
            Assert.assertEquals(SavedArticlesUiState.Loading, awaitItem())

            val successState = awaitItem()
            Assert.assertTrue(successState is SavedArticlesUiState.Success)
            Assert.assertEquals(savedArticles, (successState as SavedArticlesUiState.Success).articles)
            Assert.assertEquals(2, successState.articles.size)
            Assert.assertEquals("Saved Title 1", successState.articles[0].title)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `uiState emits Error when repository throws exception`() = runTest {
        val errorMessage = "Database Error"
        every { mockArticleRepository.getAllBookmarkedArticles() } returns flow { throw Exception(errorMessage) }
        viewModel = SavedArticlesViewModel(mockArticleRepository) // Re-initialize

        viewModel.uiState.test {
            Assert.assertEquals(SavedArticlesUiState.Loading, awaitItem())

            val errorState = awaitItem()
            Assert.assertTrue(errorState is SavedArticlesUiState.Error)
            Assert.assertEquals(errorMessage, (errorState as SavedArticlesUiState.Error).message)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `deleteSavedArticle calls repository deleteArticle`() = runTest {
        viewModel.deleteSavedArticle(savedArticleData1)
        coVerify { mockArticleRepository.deleteArticle(savedArticleData1) }
    }

    @Test
    fun `deleteAllSavedArticles calls repository deleteAllArticles`() = runTest {
        viewModel.deleteAllSavedArticles()
        coVerify { mockArticleRepository.deleteAllArticles() }
    }

    @Test
    fun `uiState updates when articles are deleted`() = runTest {
        val initialArticles = listOf(savedArticleData1, savedArticleData2)
        val articlesAfterDeletion = listOf(savedArticleData2)

        val articlesFlowController = MutableStateFlow(initialArticles)
        every { mockArticleRepository.getAllBookmarkedArticles() } returns articlesFlowController
        coEvery { mockArticleRepository.deleteArticle(savedArticleData1) } coAnswers {
            articlesFlowController.value = articlesAfterDeletion // Simulate DB update and flow re-emission
        }

        viewModel = SavedArticlesViewModel(mockArticleRepository) // Re-initialize with flow controller

        viewModel.uiState.test {
            Assert.assertEquals(SavedArticlesUiState.Loading, awaitItem()) // Initial loading

            val initialState = awaitItem() as SavedArticlesUiState.Success // Initial success
            Assert.assertEquals(2, initialState.articles.size)

            viewModel.deleteSavedArticle(savedArticleData1) // Trigger deletion

            val updatedState = awaitItem() as SavedArticlesUiState.Success // State after deletion
            Assert.assertEquals(1, updatedState.articles.size)
            Assert.assertEquals(savedArticleData2.url, updatedState.articles[0].url)

            cancelAndConsumeRemainingEvents()
        }
    }
}
