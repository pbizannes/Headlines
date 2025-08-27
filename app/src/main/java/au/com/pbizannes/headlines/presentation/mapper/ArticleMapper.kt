package au.com.pbizannes.headlines.presentation.mapper

import au.com.pbizannes.headlines.domain.model.Article
import au.com.pbizannes.headlines.presentation.components.ArticleUI
import au.com.pbizannes.headlines.util.Tools
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.time.ExperimentalTime

object ArticleMapper {
    private val outputDateFormatter by lazy {
        DateTimeFormatter.ofPattern("MMM dd, yyyy")
    }

    @OptIn(ExperimentalTime::class)
    private fun formatPublishedAt(publishedAtString: String): String {
        return try {
            val instant = Tools.then(publishedAtString)
            val now = Tools.now()
            val duration = Duration.between(instant, now)

            when {
                duration.seconds < 60 -> "${duration.seconds}s ago"
                duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
                duration.toHours() < 24 -> "${duration.toHours()}h ago"
                duration.toDays() < 7 -> "${duration.toDays()}d ago"
                else -> {
                    outputDateFormatter.format(instant)
                }
            }
        } catch (e: DateTimeParseException) {
            // Log the error or handle it gracefully
            e.printStackTrace()
            "Date unavailable" // Fallback string
        } catch (e: Exception) {
            e.printStackTrace()
            "Date unavailable"
        }
    }

    fun toPresentation(domainArticle: Article): ArticleUI {
        return ArticleUI(
            url = domainArticle.url,
            source = domainArticle.source.name,
            author = domainArticle.author,
            title = domainArticle.title,
            description = domainArticle.description,
            urlToImage = domainArticle.urlToImage,
            publishedAtFormatted = formatPublishedAt(domainArticle.publishedAt),
            content = domainArticle.content,
        )
    }

    fun toPresentationList(domainArticles: List<Article>): List<ArticleUI> {
        return domainArticles.map { toPresentation(it) }
    }
}
