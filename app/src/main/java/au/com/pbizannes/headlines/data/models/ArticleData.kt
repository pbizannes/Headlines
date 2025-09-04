package au.com.pbizannes.headlines.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "articles")
data class ArticleData(
    @PrimaryKey
    @SerialName("url")
    val url: String,

    @Embedded
    @SerialName("source")
    val source: ArticleSourceData,

    @SerialName("author")
    val author: String?, // Author can sometimes be null

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String?, // Description can sometimes be null

    @SerialName("urlToImage")
    val urlToImage: String?, // URL to image can sometimes be null

    @SerialName("publishedAt")
    val publishedAt: String,

    @SerialName("content")
    val content: String? // Content can sometimes be null
)
