package au.com.pbizannes.headlines.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleSource(
    @SerialName("id")
    val id: String?, // id can be null as seen in your JSON example

    @SerialName("name")
    val name: String
)
