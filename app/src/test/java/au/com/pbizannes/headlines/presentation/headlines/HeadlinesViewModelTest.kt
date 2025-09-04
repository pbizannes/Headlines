package au.com.pbizannes.headlines.presentation.headlines

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import au.com.pbizannes.headlines.data.models.ArticleData
import au.com.pbizannes.headlines.data.models.ArticleSourceData
import au.com.pbizannes.headlines.domain.repository.ArticleRepository
import au.com.pbizannes.headlines.domain.repository.NewsRepository
import au.com.pbizannes.headlines.domain.repository.UserPreferencesRepository
import au.com.pbizannes.headlines.presentation.HeadlinesUIState
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

@ExperimentalCoroutinesApi
class HeadlinesViewModelTest {

    private lateinit var mockNewsRepository: NewsRepository
    private lateinit var mockArticleRepository: ArticleRepository
    private lateinit var mockUserPrefsRepository: UserPreferencesRepository
    private lateinit var viewModel: HeadlinesViewModel

    // Sample Domain Data
    private val articleSource1 = ArticleSourceData(id = "src-id-1", name = "src-id-1") // Name is same as ID based on ViewModel logic
    private val articleSource2 = ArticleSourceData(id = "src-id-2", name = "src-id-2")
    private val domainArticleData1 = ArticleData("url1", articleSource1, "author1", "Title 1", "Desc 1", "img1", Instant.now().toString(), "Content1")
    private val domainArticleData2 = ArticleData("url2", articleSource2, "author2", "Title 2", "Desc 2", "img2", Instant.now().minusSeconds(60).toString(), "Content2")


    @Before
    fun setUp() {
        mockNewsRepository = mockk()
        mockArticleRepository = mockk()
        mockUserPrefsRepository = mockk()

        every { mockUserPrefsRepository.selectedSourceIdsFlow() } returns emptyFlow()
        coEvery { mockNewsRepository.getHeadlines(any()) } returns emptyFlow()

        every { mockArticleRepository.isArticleSaved(any()) } returns flowOf(false)
        coEvery { mockArticleRepository.getArticleByUrl(any()) } returns null
        coJustRun { mockArticleRepository.saveArticle(any()) }
        coJustRun { mockArticleRepository.deleteArticle(any()) }
    }

    private fun initializeViewModel() {
        viewModel = HeadlinesViewModel(mockNewsRepository, mockArticleRepository, mockUserPrefsRepository)
    }

    @Test
    fun `loadContent with no selected preferences fetches headlines with empty sources list`() = runTest {
        coEvery { mockNewsRepository.getHeadlines(emptyList()) } returns flowOf(domainArticleData1, domainArticleData2)
        initializeViewModel()

        viewModel.headlinesUIState.test {
            assertEquals("Initial state should be Loading", HeadlinesUIState.Loading, awaitItem())

            viewModel.loadContent()

            val successState = awaitItem()
            assertTrue("State should be Success", successState is HeadlinesUIState.Success)
            val successArticles = (successState as HeadlinesUIState.Success).articles

            assertEquals("Should have 1 article", 1, successArticles.size)
            assertEquals("Title should match", domainArticleData1.title, successArticles[0].title)

            cancelAndConsumeRemainingEvents()
        }
        coVerify { mockNewsRepository.getHeadlines(emptyList()) }
    }

    @Test
    fun `loadContent with selected preferences fetches headlines for those sources`() = runTest {
        val selectedIds = setOf("src-id-1", "src-id-2")
        every { mockUserPrefsRepository.selectedSourceIdsFlow() } returns flowOf(selectedIds)

        val expectedSourcesToFetch = listOf(
            ArticleSourceData(id = "src-id-1", name = "src-id-1"),
            ArticleSourceData(id = "src-id-2", name = "src-id-2")
        )
        coEvery { mockNewsRepository.getHeadlines(expectedSourcesToFetch) } returns flow {
            emit(domainArticleData1)
            emit(domainArticleData2)
        }
        initializeViewModel()

        viewModel.headlinesUIState.test {
            assertEquals(HeadlinesUIState.Loading, awaitItem())

            viewModel.loadContent()
            val successState1 = awaitItem() // After first article
            assertTrue(successState1 is HeadlinesUIState.Success)
            assertEquals(1, (successState1 as HeadlinesUIState.Success).articles.size)
            assertEquals(domainArticleData1.title, successState1.articles[0].title)

            val successState2 = awaitItem() // After second article
            assertTrue(successState2 is HeadlinesUIState.Success)
            assertEquals(2, (successState2 as HeadlinesUIState.Success).articles.size)
            assertEquals(domainArticleData1.title, successState2.articles[0].title) // First article still there
            assertEquals(domainArticleData2.title, successState2.articles[1].title) // Second article added

            cancelAndConsumeRemainingEvents()
        }
        coVerify { mockNewsRepository.getHeadlines(expectedSourcesToFetch) }
    }

    @Test
    fun `loadContent when getHeadlines flow is empty results in Success with empty list`() = runTest {
        val selectedIds = setOf("src-id-1")
        every { mockUserPrefsRepository.selectedSourceIdsFlow() } returns flowOf(selectedIds)
        val expectedSourcesToFetch = listOf(ArticleSourceData(id = "src-id-1", name = "src-id-1"))
        coEvery { mockNewsRepository.getHeadlines(expectedSourcesToFetch) } returns emptyFlow()
        initializeViewModel()

        viewModel.headlinesUIState.test {
            assertEquals(HeadlinesUIState.Loading, awaitItem())

            viewModel.loadContent()

            val successState = awaitItem()
            assertTrue(successState is HeadlinesUIState.Success)
            assertTrue((successState as HeadlinesUIState.Success).articles.isEmpty())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadContent when getHeadlines flow throws error emits Error state`() = runTest {
        val selectedIds = setOf("src-id-1")
        every { mockUserPrefsRepository.selectedSourceIdsFlow() } returns flowOf(selectedIds)
        val expectedSourcesToFetch = listOf(ArticleSourceData(id = "src-id-1", name = "src-id-1"))
        val errorMessage = "Network Failure"
        coEvery { mockNewsRepository.getHeadlines(expectedSourcesToFetch) } returns flow { throw Exception(errorMessage) }
        initializeViewModel()

        viewModel.headlinesUIState.test {
            assertEquals(HeadlinesUIState.Loading, awaitItem())

            viewModel.loadContent()
            val errorState = awaitItem()
            assertTrue(errorState is HeadlinesUIState.Error)
            assertEquals("Error fetching headlines: $errorMessage", (errorState as HeadlinesUIState.Error).message)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadContent when general exception occurs during processing emits Error state`() = runTest {
        val errorMessage = "Datastore unavailable"
        every { mockUserPrefsRepository.selectedSourceIdsFlow() } returns flow { throw RuntimeException(errorMessage) }
        initializeViewModel()

        viewModel.headlinesUIState.test {
            val firstState = awaitItem()

            viewModel.loadContent()

            val errorState = skipItemsUpToTheFirstInterestingOne()

            assertTrue("Expected Error state, but got $errorState",errorState is HeadlinesUIState.Error)
            assertEquals("Failed to load headlines: $errorMessage", (errorState as HeadlinesUIState.Error).message)

            cancelAndConsumeRemainingEvents()
        }
    }

    private suspend fun <T> ReceiveTurbine<T>.skipItemsUpToTheFirstInterestingOne(): T {
        var item = awaitItem()
        while (item is HeadlinesUIState.Loading) {
            item = awaitItem()
        }
        return item
    }


    @Test
    fun `onBookmarkCheckedChange saves article when isBookmarked is true`() = runTest {
        initializeViewModel() // Ensure ViewModel is initialized
        viewModel.onBookmarkCheckedChange(domainArticleData1, true)

        coVerify { mockArticleRepository.saveArticle(domainArticleData1) }
        coVerify(exactly = 0) { mockArticleRepository.deleteArticle(any()) }
    }

    @Test
    fun `onBookmarkCheckedChange deletes article when isBookmarked is false and article exists in repo`() = runTest {
        initializeViewModel()
        coEvery { mockArticleRepository.getArticleByUrl(domainArticleData1.url) } returns domainArticleData1

        viewModel.onBookmarkCheckedChange(domainArticleData1, false)

        coVerify { mockArticleRepository.deleteArticle(domainArticleData1) }
        coVerify(exactly = 0) { mockArticleRepository.saveArticle(any()) }
    }

    @Test
    fun `onBookmarkCheckedChange does not delete if article not found for deletion`() = runTest {
        initializeViewModel()
        coEvery { mockArticleRepository.getArticleByUrl(domainArticleData1.url) } returns null

        viewModel.onBookmarkCheckedChange(domainArticleData1, false)

        coVerify(exactly = 0) { mockArticleRepository.deleteArticle(any()) }
    }

    @Test
    fun `isArticleBookmarked returns correct flow from repository`() = runTest {
        initializeViewModel()
        val testUrl = "url_test"

        every { mockArticleRepository.isArticleSaved(testUrl) } returns flowOf(true)
        viewModel.isArticleBookmarked(testUrl).test {
            assertTrue("Article should be bookmarked", awaitItem())
            awaitComplete()
        }

        every { mockArticleRepository.isArticleSaved(testUrl) } returns flowOf(false)
        viewModel.isArticleBookmarked(testUrl).test {
            assertEquals("Article should not be bookmarked", false, awaitItem())
            awaitComplete()
        }
        coVerify(exactly = 2) { mockArticleRepository.isArticleSaved(testUrl) }
    }
}
