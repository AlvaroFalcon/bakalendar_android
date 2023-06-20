package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class AnimeResponse(
    @SerializedName("data") val data: Anime
)
