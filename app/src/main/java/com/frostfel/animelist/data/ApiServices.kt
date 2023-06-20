package com.frostfel.animelist.data

import com.frostfel.animelist.model.AnimeResponse
import com.frostfel.animelist.model.SeasonResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServices {
    @GET("seasons/now")
    suspend fun getCurrentSeason(
        @Query("page") page: Int
    ): SeasonResponse

    @GET("anime/{id}")
    suspend fun getAnimeById(
        @Path(value="id") id: Int
    ) : AnimeResponse
}