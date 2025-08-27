package au.com.pbizannes.headlines.presentation.mapper

import au.com.pbizannes.headlines.domain.model.Article
import au.com.pbizannes.headlines.domain.model.ArticleSource
import org.junit.Assert
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class ArticleMapperTest {

    @Test
    fun `toPresentation maps domain Article to ArticleUi correctly`() {
        val domainSource = ArticleSource(id = "test-source", name = "Test Source Name")
        val now = Instant.now()
        val domainArticle = Article(
            url = "http://example.com/article1",
            source = domainSource,
            author = "Test Author",
            title = "Test Title",
            description = "Test Description",
            urlToImage = "http://example.com/image.jpg",
            publishedAt = "2025-08-27T00:55:20Z",
            content = "Test Content"
        )

        val articleUi = ArticleMapper.toPresentation(domainArticle)

        Assert.assertEquals("http://example.com/article1", articleUi.url)
        Assert.assertEquals("Test Source Name", articleUi.source)
        Assert.assertEquals("Test Author", articleUi.author)
        Assert.assertEquals("Test Title", articleUi.title)
        Assert.assertEquals("Test Description", articleUi.description)
        Assert.assertEquals("http://example.com/image.jpg", articleUi.urlToImage)
        Assert.assertEquals("Test Content", articleUi.content)
        // More specific date testing would require mocking Instant.now() or careful setup
        Assert.assertEquals("6h ago", articleUi.publishedAtFormatted) // Assuming API >= 26
    }

    @Test
    fun `toPresentation formats date a few hours ago`() {
        val domainSource = ArticleSource(id = "test-source", name = "Test Source Name")
        val fewHoursAgo = Instant.now().minus(3, ChronoUnit.HOURS).toString()
        val domainArticle = Article(
            url = "http://example.com/article2",
            source = domainSource,
            author = null,
            title = "Another Title",
            description = null,
            urlToImage = null,
            publishedAt = fewHoursAgo,
            content = null
        )

        val articleUi = ArticleMapper.toPresentation(domainArticle)

        Assert.assertEquals("3h ago", articleUi.publishedAtFormatted) // Assuming API >= 26
    }


    @Test
    fun `toPresentation formats date many days ago`() {
        val domainSource = ArticleSource(id = "test-source", name = "Test Source Name")
        val manyDaysAgo = Instant.now().minus(10, ChronoUnit.DAYS).toString()
        val domainArticle = Article(
            url = "http://example.com/article3",
            source = domainSource,
            author = "Author",
            title = "Old News",
            description = "Desc",
            urlToImage = "img.jpg",
            publishedAt = manyDaysAgo,
            content = "Content"
        )

        val articleUi = ArticleMapper.toPresentation(domainArticle)

        // This will be a formatted date like "Oct 17, 2023" (depending on current date)
        // For a stable test, you might mock the date or check for a pattern.
        // For simplicity, we check it's not a relative time string.
        assert(!articleUi.publishedAtFormatted.endsWith("ago"))
    }

    @Test
    fun `toPresentationList maps list of domain Articles correctly`() {
        val domainSource = ArticleSource(id = "test-source", name = "Test Source Name")
        val domainArticles = listOf(
            Article(
                url = "url1", source = domainSource, author = "A1", title = "T1",
                description = "D1", urlToImage = "I1", publishedAt = Instant.now().toString(), content = "C1"
            ),
            Article(
                url = "url2", source = domainSource, author = "A2", title = "T2",
                description = "D2", urlToImage = "I2", publishedAt = Instant.now().minus(1, ChronoUnit.DAYS).toString(), content = "C2"
            )
        )

        val articleUiList = ArticleMapper.toPresentationList(domainArticles)

        Assert.assertEquals(2, articleUiList.size)
        Assert.assertEquals("url1", articleUiList[0].url)
        Assert.assertEquals("T1", articleUiList[0].title)
        Assert.assertEquals("0s ago", articleUiList[0].publishedAtFormatted)

        Assert.assertEquals("url2", articleUiList[1].url)
        Assert.assertEquals("T2", articleUiList[1].title)
        Assert.assertEquals("1d ago", articleUiList[1].publishedAtFormatted)
    }
}
