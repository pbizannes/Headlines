package au.com.pbizannes.headlines.presentation.mapper

import au.com.pbizannes.headlines.data.models.ArticleData
import au.com.pbizannes.headlines.domain.models.Article
import au.com.pbizannes.headlines.presentation.components.ArticleUI
import au.com.pbizannes.headlines.util.Tools
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.time.ExperimentalTime

object ArticleMapper {
    private val outputDateFormatter by lazy {
        DateTimeFormatter.ofPattern("MMM dd, yyyy")
    }

    @OptIn(ExperimentalTime::class)
    private fun formatPublishedAt(publishedAt: Instant): String {
        return try {
            val now = Tools.now()
            val duration = Duration.between(publishedAt, now)

            when {
                duration.seconds < 60 -> "${duration.seconds}s ago"
                duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
                duration.toHours() < 24 -> "${duration.toHours()}h ago"
                duration.toDays() < 7 -> "${duration.toDays()}d ago"
                else -> {
                    outputDateFormatter.format(publishedAt)
                }
            }
        } catch (e: DateTimeParseException) {
            e.printStackTrace()
            "Date unavailable"
        } catch (e: Exception) {
            e.printStackTrace()
            "Date unavailable"
        }
    }

    fun toPresentation(domainArticleData: Article): ArticleUI {
        return ArticleUI(
            url = domainArticleData.url,
            source = domainArticleData.source.name,
            author = domainArticleData.author,
            title = domainArticleData.title,
            description = domainArticleData.description,
            urlToImage = domainArticleData.urlToImage,
            publishedAtFormatted = formatPublishedAt(domainArticleData.publishedAt),
            content = domainArticleData.content,
        )
    }

    fun toPresentationList(domainArticleEntities: List<Article>): List<ArticleUI> {
        return domainArticleEntities.map { toPresentation(it) }
    }
}
