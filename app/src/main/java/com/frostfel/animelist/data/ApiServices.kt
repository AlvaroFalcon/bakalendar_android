package com.frostfel.animelist.data

import retrofit2.http.GET

interface ApiServices {
    @GET("seasons/now")
    suspend fun getCurrentSeason(): Any
}