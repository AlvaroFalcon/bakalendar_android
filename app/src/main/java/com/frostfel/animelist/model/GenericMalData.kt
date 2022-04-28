package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class GenericMalData(
    @SerializedName("mal_id") val malId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String,
)