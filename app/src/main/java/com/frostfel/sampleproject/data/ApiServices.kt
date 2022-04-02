package com.frostfel.sampleproject.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiServices {
    @POST("call/with_body")
    suspend fun authenticateUser(@Body userAuthRequest: Any): Any

    @POST("call/with_query")
    suspend fun resetPassword(@Query("user_email") email: String)
}