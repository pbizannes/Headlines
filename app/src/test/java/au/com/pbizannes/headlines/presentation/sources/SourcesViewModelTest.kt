package au.com.pbizannes.headlines.presentation.sources

import app.cash.turbine.test
import au.com.pbizannes.headlines.data.models.NewsSourceData
import au.com.pbizannes.headlines.domain.repository.NewsRepository
import au.com.pbizannes.headlines.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SourcesViewModelTest {

    private lateinit var mockNewsRepository: NewsRepository
    private lateinit var mockUserPrefsRepository: UserPreferencesRepository
    private lateinit var viewModel: SourcesViewModel

    // Sample Data
    private val source1 = NewsSourceData("src1", "Source One", "Desc1", "url1", "cat1", "lang1", "country1")
    private val source2 = NewsSourceData("src2", "Source Two", "Desc2", "url2", "cat2", "lang2", "country2")
    private val source3 = NewsSourceData("src3", "Source Three", "Desc3", "url3", "cat3", "lang3", "country3")
    private val allDomainSources = listOf(source1, source2, source3)

    @Before
    fun setUp() {
        mockNewsRepository = mockk()
        mockUserPrefsRepository = mockk()

        coEvery { mockNewsRepository.getSources() } returns Result.success(allDomainSources)
        every { mockUserPrefsRepository.selectedSourceIdsFlow() } returns flowOf(emptySet()) // No selection by default
        coJustRun { mockUserPrefsRepository.addSourceId(any()) }
        coJustRun { mockUserPrefsRepository.removeSourceId(any()) }
        coJustRun { mockUserPrefsRepository.updateSelectedSourceIds(any()) }
        coJustRun { mockUserPrefsRepository.clearSelectedSourceIds() }


        viewModel = SourcesViewModel(mockNewsRepository, mockUserPrefsRepository)
    }

    @Test
    fun `uiState emits Loading then Success with all sources unselected initially`() = runTest {
        viewModel.uiState.test {
            Assert.assertEquals("Initial state should be Loading", SourcesScreenUiState.Loading, awaitItem())

            val successState = awaitItem()
            Assert.assertTrue("State should be Success", successState is SourcesScreenUiState.Success)
            val sourceItems = (successState as SourcesScreenUiState.Success).sourceItems

            Assert.assertEquals("Should have all sources", 3, sourceItems.size)
            Assert.assertTrue("All sources should be unselected by default", sourceItems.all { !it.isSelected })
            Assert.assertEquals(source1.name, sourceItems[0].source.name)
            Assert.assertEquals(source2.name, sourceItems[1].source.name)
            Assert.assertEquals(source3.name, sourceItems[2].source.name)

            cancelAndConsumeRemainingEvents()
        }
        coVerify { mockNewsRepository.getSources() }
        verify { mockUserPrefsRepository.selectedSourceIdsFlow() }
    }

    @Test
    fun `uiState reflects selected sources from preferences`() = runTest {
        val selectedIds = setOf("src1", "src3")
        every { mockUserPrefsRepository.selectedSourceIdsFlow() } returns flowOf(selectedIds)

        viewModel = SourcesViewModel(mockNewsRepository, mockUserPrefsRepository)

        viewModel.uiState.test {
            Assert.assertEquals(SourcesScreenUiState.Loading, awaitItem())

            val successState = awaitItem()
            Assert.assertTrue(successState is SourcesScreenUiState.Success)
            val sourceItems = (successState as SourcesScreenUiState.Success).sourceItems

            Assert.assertEquals(3, sourceItems.size)
            val item1 = sourceItems.find { it.source.id == "src1" }
            val item2 = sourceItems.find { it.source.id == "src2" }
            val item3 = sourceItems.find { it.source.id == "src3" }

            Assert.assertTrue("Source 1 should be selected", item1?.isSelected == true)
            Assert.assertTrue("Source 2 should NOT be selected", item2?.isSelected == false)
            Assert.assertTrue("Source 3 should be selected", item3?.isSelected == true)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `uiState emits Error when newsRepository getSources fails`() = runTest {
        val errorMessage = "Network Error Fetching Sources"
        coEvery { mockNewsRepository.getSources() } returns Result.failure(Exception(errorMessage))
        viewModel = SourcesViewModel(mockNewsRepository, mockUserPrefsRepository) // Re-initialize

        viewModel.uiState.test {
            Assert.assertEquals(SourcesScreenUiState.Loading, awaitItem())

            val errorState = awaitItem()
            Assert.assertTrue(errorState is SourcesScreenUiState.Error)
            Assert.assertEquals(errorMessage, (errorState as SourcesScreenUiState.Error).message)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `uiState emits Error when selectedSourceIdsFlow emits error`() = runTest {
        val errorMessage = "Preferences Error"
        every { mockUserPrefsRepository.selectedSourceIdsFlow() } returns flow { throw Exception(errorMessage) }
        viewModel = SourcesViewModel(mockNewsRepository, mockUserPrefsRepository) // Re-initialize

        viewModel.uiState.test {
            Assert.assertEquals(SourcesScreenUiState.Loading, awaitItem())

            val errorState = awaitItem()
            Assert.assertTrue(errorState is SourcesScreenUiState.Error)
            Assert.assertEquals(errorMessage, (errorState as SourcesScreenUiState.Error).message)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `onSourceSelectedChange calls addSourceId when isSelected is true`() = runTest {
        val sourceIdToSelect = "src1"
        viewModel.onSourceSelectedChange(sourceIdToSelect, true)
        coVerify { mockUserPrefsRepository.addSourceId(sourceIdToSelect) }
        coVerify(exactly = 0) { mockUserPrefsRepository.removeSourceId(any()) }
    }

    @Test
    fun `onSourceSelectedChange calls removeSourceId when isSelected is false`() = runTest {
        val sourceIdToDeselect = "src2"
        viewModel.onSourceSelectedChange(sourceIdToDeselect, false)
        coVerify { mockUserPrefsRepository.removeSourceId(sourceIdToDeselect) }
        coVerify(exactly = 0) { mockUserPrefsRepository.addSourceId(any()) }
    }

    @Test
    fun `selectAllSources updates preferences with all source IDs`() = runTest {
        every { mockUserPrefsRepository.selectedSourceIdsFlow() } returns flowOf(emptySet()) // start with none selected
        viewModel = SourcesViewModel(mockNewsRepository, mockUserPrefsRepository)
        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Initial Success
            cancelAndIgnoreRemainingEvents()
        }


        viewModel.selectAllSources()

        val expectedAllIds = allDomainSources.mapNotNull { it.id }.toSet()
        coVerify { mockUserPrefsRepository.updateSelectedSourceIds(expectedAllIds) }
    }

    @Test
    fun `deselectAllSources calls clearSelectedSourceIds`() = runTest {
        viewModel.deselectAllSources()
        coVerify { mockUserPrefsRepository.clearSelectedSourceIds() }
    }
}

