package au.com.pbizannes.headlines.presentation.mapper

import au.com.pbizannes.headlines.data.models.ArticleData
import au.com.pbizannes.headlines.data.models.ArticleSourceData
import au.com.pbizannes.headlines.util.Tools
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class ArticleMapperTest {
    @Before
    fun setup() {
        mockkObject(Tools)
        every { Tools.now() } returns Instant.parse("2025-08-27T00:55:20Z")
    }

    @Test
    fun `toPresentation maps domain Article to ArticleUi correctly`() {
        val domainSource = ArticleSourceData(id = "test-source", name = "Test Source Name")
        val domainArticleData = ArticleData(
            url = "http://example.com/article1",
            source = domainSource,
            author = "Test Author",
            title = "Test Title",
            description = "Test Description",
            urlToImage = "http://example.com/image.jpg",
            publishedAt = "2025-08-26T18:55:20Z",
            content = "Test Content"
        )

        val articleUi = ArticleMapper.toPresentation(domainArticleData)

        Assert.assertEquals("http://example.com/article1", articleUi.url)
        Assert.assertEquals("Test Source Name", articleUi.source)
        Assert.assertEquals("Test Author", articleUi.author)
        Assert.assertEquals("Test Title", articleUi.title)
        Assert.assertEquals("Test Description", articleUi.description)
        Assert.assertEquals("http://example.com/image.jpg", articleUi.urlToImage)
        Assert.assertEquals("Test Content", articleUi.content)
        Assert.assertEquals("6h ago", articleUi.publishedAtFormatted) // Assuming API >= 26
    }

    @Test
    fun `toPresentation formats date a few hours ago`() {
        val domainSource = ArticleSourceData(id = "test-source", name = "Test Source Name")
        val fewHoursAgo = Tools.now().minus(3, ChronoUnit.HOURS).toString()
        val domainArticleData = ArticleData(
            url = "http://example.com/article2",
            source = domainSource,
            author = null,
            title = "Another Title",
            description = null,
            urlToImage = null,
            publishedAt = fewHoursAgo,
            content = null
        )

        val articleUi = ArticleMapper.toPresentation(domainArticleData)

        Assert.assertEquals("3h ago", articleUi.publishedAtFormatted) // Assuming API >= 26
    }


    @Test
    fun `toPresentation formats date many days ago`() {
        val domainSource = ArticleSourceData(id = "test-source", name = "Test Source Name")
        val manyDaysAgo = Tools.now().minus(10, ChronoUnit.DAYS).toString()
        val domainArticleData = ArticleData(
            url = "http://example.com/article3",
            source = domainSource,
            author = "Author",
            title = "Old News",
            description = "Desc",
            urlToImage = "img.jpg",
            publishedAt = manyDaysAgo,
            content = "Content"
        )

        val articleUi = ArticleMapper.toPresentation(domainArticleData)

        assert(!articleUi.publishedAtFormatted.endsWith("ago"))
    }

    @Test
    fun `toPresentationList maps list of domain Articles correctly`() {
        val domainSource = ArticleSourceData(id = "test-source", name = "Test Source Name")
        val domainArticleEntities = listOf(
            ArticleData(
                url = "url1", source = domainSource, author = "A1", title = "T1",
                description = "D1", urlToImage = "I1", publishedAt = Tools.now().toString(), content = "C1"
            ),
            ArticleData(
                url = "url2", source = domainSource, author = "A2", title = "T2",
                description = "D2", urlToImage = "I2", publishedAt = Tools.now().minus(1, ChronoUnit.DAYS).toString(), content = "C2"
            )
        )

        val articleUiList = ArticleMapper.toPresentationList(domainArticleEntities)

        Assert.assertEquals(2, articleUiList.size)
        Assert.assertEquals("url1", articleUiList[0].url)
        Assert.assertEquals("T1", articleUiList[0].title)
        Assert.assertEquals("0s ago", articleUiList[0].publishedAtFormatted)

        Assert.assertEquals("url2", articleUiList[1].url)
        Assert.assertEquals("T2", articleUiList[1].title)
        Assert.assertEquals("1d ago", articleUiList[1].publishedAtFormatted)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
