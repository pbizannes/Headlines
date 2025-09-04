package au.com.pbizannes.headlines.data

import app.cash.turbine.test
import au.com.pbizannes.headlines.data.source.local.ArticleDao
import au.com.pbizannes.headlines.data.models.ArticleData
import au.com.pbizannes.headlines.data.models.ArticleSourceData
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.Instant

class DefaultArticleRepositoryTest {

    private lateinit var mockArticleDao: ArticleDao
    private lateinit var repository: DefaultArticleRepository

    private val testSource = ArticleSourceData("src1", "Source 1")
    private val testArticleData = ArticleData("url1", testSource, "author", "title", "desc", "img", Instant.now().toString(), "content")

    @Before
    fun setUp() {
        mockArticleDao = mockk()
        // Default behavior for DAO mocks
        coJustRun { mockArticleDao.insertArticle(any()) }
        coJustRun { mockArticleDao.deleteArticle(any()) }
        every { mockArticleDao.isArticleBookmarked(any()) } returns flowOf(false)
        coEvery { mockArticleDao.getArticleByUrl(any()) } returns null
        every { mockArticleDao.getAllArticles() } returns flowOf(emptyList())

        repository = DefaultArticleRepository(mockArticleDao)
    }

    @Test
    fun `saveArticle calls dao insertArticle`() = runTest {
        repository.saveArticle(testArticleData)
        coVerify { mockArticleDao.insertArticle(testArticleData) }
    }

    @Test
    fun `deleteArticle calls dao deleteArticle`() = runTest {
        repository.deleteArticle(testArticleData)
        coVerify { mockArticleDao.deleteArticle(testArticleData) }
    }

    @Test
    fun `isArticleSaved returns flow from dao isArticleBookmarked`() = runTest {
        val testUrl = "test_url"
        every { mockArticleDao.isArticleBookmarked(testUrl) } returns flowOf(true)

        repository.isArticleSaved(testUrl).test {
            Assert.assertTrue(awaitItem())
            awaitComplete()
        }

        every { mockArticleDao.isArticleBookmarked(testUrl) } returns flowOf(false)
        repository.isArticleSaved(testUrl).test {
            Assert.assertEquals(false, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getArticleByUrl calls dao getArticleByUrl and returns result`() = runTest {
        val testUrl = "test_url"
        coEvery { mockArticleDao.getArticleByUrl(testUrl) } returns testArticleData

        val result = repository.getArticleByUrl(testUrl)

        Assert.assertEquals(testArticleData, result)
        coVerify { mockArticleDao.getArticleByUrl(testUrl) }
    }


    @Test
    fun `getAllSavedArticles returns flow from dao getAllArticles`() = runTest {
        val articles = listOf(testArticleData)
        every { mockArticleDao.getAllArticles() } returns flowOf(articles)

        repository.getAllBookmarkedArticles().test {
            val emittedList = awaitItem()
            Assert.assertEquals(1, emittedList.size)
            Assert.assertEquals(testArticleData.url, emittedList[0].url)
            awaitComplete()
        }
    }
}
