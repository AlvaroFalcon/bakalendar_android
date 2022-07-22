package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class SeasonResponse(
    @SerializedName("data") val data: List<Anime>,
    @SerializedName("pagination") val pagination: Pagination
)
