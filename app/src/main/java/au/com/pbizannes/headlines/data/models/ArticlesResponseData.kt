package au.com.pbizannes.headlines.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticlesResponseData(
    @SerialName("status")
    val status: String,

    @SerialName("totalResults")
    val totalResults: Int,

    @SerialName("articles")
    val articleEntities: List<ArticleData>
)
