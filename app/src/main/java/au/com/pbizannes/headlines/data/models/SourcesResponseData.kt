package au.com.pbizannes.headlines.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SourcesResponseData(
    @SerialName("status")
    val status: String,

    @SerialName("sources")
    val sources: List<NewsSourceData>
)
