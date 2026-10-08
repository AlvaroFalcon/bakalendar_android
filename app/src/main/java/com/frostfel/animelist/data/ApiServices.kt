package com.frostfel.animelist.data

import com.frostfel.animelist.model.AnimeResponse
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

    @GET("anime/{id}")
    suspend fun getAnimeById(
        @Path(value="id") id: Int
    ) : AnimeResponse
}