package com.frostfel.animelist.data

import com.frostfel.animelist.model.SeasonResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {
    @GET("seasons/now")
    suspend fun getCurrentSeason(
        @Query("page") page: Int
    ): SeasonResponse
}