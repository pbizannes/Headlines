package au.com.pbizannes.headlines.presentation.headlines

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import au.com.pbizannes.headlines.data.preferences.UserPreferencesRepository
import au.com.pbizannes.headlines.domain.model.Article
import au.com.pbizannes.headlines.domain.model.ArticleSource
import au.com.pbizannes.headlines.domain.repository.ArticleRepository
import au.com.pbizannes.headlines.domain.repository.NewsRepository
import au.com.pbizannes.headlines.presentation.HeadlinesUIState
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
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
    private val articleSource1 = ArticleSource(id = "src-id-1", name = "src-id-1") // Name is same as ID based on ViewModel logic
    private val articleSource2 = ArticleSource(id = "src-id-2", name = "src-id-2")
    private val domainArticle1 = Article("url1", articleSource1, "author1", "Title 1", "Desc 1", "img1", Instant.now().toString(), "Content1")
    private val domainArticle2 = Article("url2", articleSource2, "author2", "Title 2", "Desc 2", "img2", Instant.now().minusSeconds(60).toString(), "Content2")


    @Before
    fun setUp() {
        mockNewsRepository = mockk()
        mockArticleRepository = mockk()
        mockUserPrefsRepository = mockk()

        // --- Default Mock Behaviors ---
        every { mockUserPrefsRepository.selectedSourceIdsFlow } returns flowOf(emptySet()) // Default: no specific sources selected
        // Default: getHeadlines returns no articles for any source list
        coEvery { mockNewsRepository.getHeadlines(any()) } returns emptyFlow()

        every { mockArticleRepository.isArticleSaved(any()) } returns flowOf(false)
        coEvery { mockArticleRepository.getArticleByUrl(any()) } returns null
        coJustRun { mockArticleRepository.saveArticle(any()) }
        coJustRun { mockArticleRepository.deleteArticle(any()) }

        // ViewModel initialization happens after mocks are set up for default behavior
        // The loadContent() will be called during initialization.
    }

    private fun initializeViewModel() {
        viewModel = HeadlinesViewModel(mockNewsRepository, mockArticleRepository, mockUserPrefsRepository)
    }

    @Test
    fun `loadContent with no selected preferences fetches headlines with empty sources list`() = runTest {
        // Arrange: selectedSourceIdsFlow emits emptySet (default setup)
        // newsRepository.getHeadlines should be called with an empty list of ArticleSource
        coEvery { mockNewsRepository.getHeadlines(emptyList()) } returns flowOf(domainArticle1) // Simulate one article for empty sources list (if API supports this, or adjust assertion)
        initializeViewModel()

        viewModel.headlinesUIState.test {
            assertEquals("Initial state should be Loading", HeadlinesUIState.Loading, awaitItem())

            val successState = awaitItem()
            assertTrue("State should be Success", successState is HeadlinesUIState.Success)
            val successArticles = (successState as HeadlinesUIState.Success).articles // These are domain Articles

            assertEquals("Should have 1 article", 1, successArticles.size)
            assertEquals("Title should match", domainArticle1.title, successArticles[0].title)

            cancelAndConsumeRemainingEvents()
        }
        coVerify { mockUserPrefsRepository.selectedSourceIdsFlow.firstOrNull() }
        coVerify { mockNewsRepository.getHeadlines(emptyList()) }
    }

    @Test
    fun `loadContent with selected preferences fetches headlines for those sources`() = runTest {
        // Arrange
        val selectedIds = setOf("src-id-1", "src-id-2")
        every { mockUserPrefsRepository.selectedSourceIdsFlow } returns flowOf(selectedIds)

        val expectedSourcesToFetch = listOf(
            ArticleSource(id = "src-id-1", name = "src-id-1"),
            ArticleSource(id = "src-id-2", name = "src-id-2")
        )
        // Simulate getHeadlines returning a flow that emits articles one by one
        coEvery { mockNewsRepository.getHeadlines(expectedSourcesToFetch) } returns flow {
            emit(domainArticle1)
            emit(domainArticle2)
        }
        initializeViewModel()
        viewModel.loadContent()

        viewModel.headlinesUIState.test {
            assertEquals(HeadlinesUIState.Loading, awaitItem())

            // Since articles are collected one by one, UIState will update for each
            val successState1 = awaitItem() // After first article
            assertTrue(successState1 is HeadlinesUIState.Success)
            assertEquals(1, (successState1 as HeadlinesUIState.Success).articles.size)
            assertEquals(domainArticle1.title, successState1.articles[0].title)

            val successState2 = awaitItem() // After second article
            assertTrue(successState2 is HeadlinesUIState.Success)
            assertEquals(2, (successState2 as HeadlinesUIState.Success).articles.size)
            assertEquals(domainArticle1.title, successState2.articles[0].title) // First article still there
            assertEquals(domainArticle2.title, successState2.articles[1].title) // Second article added

            cancelAndConsumeRemainingEvents()
        }
        coVerify { mockUserPrefsRepository.selectedSourceIdsFlow.firstOrNull() }
        coVerify { mockNewsRepository.getHeadlines(expectedSourcesToFetch) }
    }

    @Test
    fun `loadContent when getHeadlines flow is empty results in Success with empty list`() = runTest {
        // Arrange
        val selectedIds = setOf("src-id-1")
        every { mockUserPrefsRepository.selectedSourceIdsFlow } returns flowOf(selectedIds)
        val expectedSourcesToFetch = listOf(ArticleSource(id = "src-id-1", name = "src-id-1"))
        coEvery { mockNewsRepository.getHeadlines(expectedSourcesToFetch) } returns emptyFlow() // No articles
        initializeViewModel()

        viewModel.headlinesUIState.test {
            assertEquals(HeadlinesUIState.Loading, awaitItem())

            val successState = awaitItem()
            assertTrue(successState is HeadlinesUIState.Success)
            assertTrue((successState as HeadlinesUIState.Success).articles.isEmpty())

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadContent when getHeadlines flow throws error emits Error state`() = runTest {
        // Arrange
        val selectedIds = setOf("src-id-1")
        every { mockUserPrefsRepository.selectedSourceIdsFlow } returns flowOf(selectedIds)
        val expectedSourcesToFetch = listOf(ArticleSource(id = "src-id-1", name = "src-id-1"))
        val errorMessage = "Network Failure"
        coEvery { mockNewsRepository.getHeadlines(expectedSourcesToFetch) } returns flow { throw Exception(errorMessage) }
        initializeViewModel()

        viewModel.headlinesUIState.test {
            assertEquals(HeadlinesUIState.Loading, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is HeadlinesUIState.Error)
            assertEquals("Error fetching headlines: $errorMessage", (errorState as HeadlinesUIState.Error).message)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `loadContent when general exception occurs during processing emits Error state`() = runTest {
        // Arrange: Make userPreferences throw an unexpected error
        val errorMessage = "Datastore unavailable"
        every { mockUserPrefsRepository.selectedSourceIdsFlow } returns flow { throw RuntimeException(errorMessage) }
        initializeViewModel()


        viewModel.headlinesUIState.test {
            // Initial state is Loading due to `_headlinesUIState` constructor
            // The `loadContent` is launched in `init`, and `viewModelScope.launch` might start after this test begins collecting.
            // Depending on dispatcher timing, we might see the initial Loading or the Loading set at the start of loadContent.
            // Let's ensure we see the Loading set by loadContent
            val firstState = awaitItem()
            // It could be the initial Loading or the one set by loadContent if loadContent starts very fast.
            // Let's be flexible or skip this exact initial state check if timing is tricky with immediate launch.
            // For robustness, usually, the Loading at the start of the tested function is the one we care about.

            // The exception in `userPreferencesRepository.selectedSourceIdsFlow.firstOrNull()` will be caught
            // by the outer try-catch in `loadContent`.
            val errorState = skipItemsUpToTheFirstInterestingOne() // Skip initial loading(s) if any

            assertTrue("Expected Error state, but got $errorState",errorState is HeadlinesUIState.Error)
            assertEquals("Failed to load headlines: $errorMessage", (errorState as HeadlinesUIState.Error).message)

            cancelAndConsumeRemainingEvents()
        }
    }

    // Helper for skipping initial/intermediate states when focusing on a later one
    private suspend fun <T> ReceiveTurbine<T>.skipItemsUpToTheFirstInterestingOne(): T {
        var item = awaitItem()
        while (item is HeadlinesUIState.Loading) { // Example condition, adjust if needed
            item = awaitItem()
        }
        return item
    }


    @Test
    fun `onBookmarkCheckedChange saves article when isBookmarked is true`() = runTest {
        initializeViewModel() // Ensure ViewModel is initialized
        // Act
        viewModel.onBookmarkCheckedChange(domainArticle1, true)

        // Assert
        coVerify { mockArticleRepository.saveArticle(domainArticle1) }
        coVerify(exactly = 0) { mockArticleRepository.deleteArticle(any()) }
    }

    @Test
    fun `onBookmarkCheckedChange deletes article when isBookmarked is false and article exists in repo`() = runTest {
        initializeViewModel()
        // Arrange
        coEvery { mockArticleRepository.getArticleByUrl(domainArticle1.url) } returns domainArticle1

        // Act
        viewModel.onBookmarkCheckedChange(domainArticle1, false)

        // Assert
        coVerify { mockArticleRepository.deleteArticle(domainArticle1) }
        coVerify(exactly = 0) { mockArticleRepository.saveArticle(any()) }
    }

    @Test
    fun `onBookmarkCheckedChange does not delete if article not found for deletion`() = runTest {
        initializeViewModel()
        // Arrange
        coEvery { mockArticleRepository.getArticleByUrl(domainArticle1.url) } returns null // Article not found

        // Act
        viewModel.onBookmarkCheckedChange(domainArticle1, false)

        // Assert
        coVerify(exactly = 0) { mockArticleRepository.deleteArticle(any()) }
    }

    @Test
    fun `isArticleBookmarked returns correct flow from repository`() = runTest {
        initializeViewModel()
        val testUrl = "url_test"
        // Test case 1: Bookmarked
        every { mockArticleRepository.isArticleSaved(testUrl) } returns flowOf(true)
        viewModel.isArticleBookmarked(testUrl).test {
            assertTrue("Article should be bookmarked", awaitItem())
            awaitComplete()
        }

        // Test case 2: Not bookmarked
        every { mockArticleRepository.isArticleSaved(testUrl) } returns flowOf(false)
        viewModel.isArticleBookmarked(testUrl).test {
            assertEquals("Article should not be bookmarked", false, awaitItem())
            awaitComplete()
        }
        coVerify(exactly = 2) { mockArticleRepository.isArticleSaved(testUrl) }
    }
}
