package com.frostfel.animelist.data

import com.frostfel.animelist.model.SeasonResponse
import retrofit2.http.GET

interface ApiServices {
    @GET("seasons/now")
    suspend fun getCurrentSeason(): SeasonResponse
}