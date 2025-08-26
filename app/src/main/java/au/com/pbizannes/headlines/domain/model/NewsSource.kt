package au.com.pbizannes.headlines.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsSource(
    @SerialName("id")
    val id: String?, // Making it nullable as sometimes IDs can be missing or null in APIs

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String,

    @SerialName("url")
    val url: String,

    @SerialName("category")
    val category: String,

    @SerialName("language")
    val language: String,

    @SerialName("country")
    val country: String
)
