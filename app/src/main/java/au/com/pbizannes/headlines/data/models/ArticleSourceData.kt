package au.com.pbizannes.headlines.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleSourceData(
    @SerialName("id")
    val id: String?, // id can be null as seen in the JSON

    @SerialName("name")
    val name: String
)
