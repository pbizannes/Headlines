package au.com.pbizannes.headlines.data.source.network

import au.com.pbizannes.headlines.domain.model.ArticlesResponse
import au.com.pbizannes.headlines.domain.model.SourcesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewsService {
    @GET("top-headlines/sources")
    suspend fun getSources(
        @Header("X-Api-Key") apiKey: String
    ): Response<SourcesResponse> //

    @GET("top-headlines")
    suspend fun getHeadlines(
        @Header("X-Api-Key") apiKey: String,
        @Query("sources") sourceList: List<String>,
        @Query("country") country: String? = null
    ): Response<ArticlesResponse> //
}
