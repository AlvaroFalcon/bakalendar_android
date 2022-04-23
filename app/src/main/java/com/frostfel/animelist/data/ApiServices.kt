package com.frostfel.animelist.data

import retrofit2.http.POST

interface ApiServices {
    @POST("seasons/now")
    suspend fun authenticateUser(): Any
}