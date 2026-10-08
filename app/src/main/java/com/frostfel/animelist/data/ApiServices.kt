package com.frostfel.animelist.data

import com.frostfel.animelist.model.AnimeResponse
import com.frostfel.animelist.model.EpisodesResponse
import com.frostfel.animelist.model.RecommendationsResponse
import com.frostfel.animelist.model.StreamingResponse
import com.frostfel.animelist.model.RelationsResponse
import com.frostfel.animelist.model.SeasonResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServices {
    @GET("seasons/now")
    suspend fun getCurrentSeason(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 25,
        // Also returns series that started in a previous season and are still airing.
        @Query("continuing") continuing: Boolean = true
    ): SeasonResponse

    @GET("anime/{id}/relations")
    suspend fun getAnimeRelations(
        @Path(value = "id") id: Int
    ): RelationsResponse

    @GET("anime/{id}/recommendations")
    suspend fun getAnimeRecommendations(
        @Path(value = "id") id: Int
    ): RecommendationsResponse

    @GET("anime/{id}/episodes")
    suspend fun getAnimeEpisodes(
        @Path(value = "id") id: Int,
        @Query("page") page: Int = 1
    ): EpisodesResponse

    @GET("anime/{id}/streaming")
    suspend fun getAnimeStreaming(
        @Path(value = "id") id: Int
    ): StreamingResponse

    @GET("anime/{id}")
    suspend fun getAnimeById(
        @Path(value="id") id: Int
    ) : AnimeResponse
}