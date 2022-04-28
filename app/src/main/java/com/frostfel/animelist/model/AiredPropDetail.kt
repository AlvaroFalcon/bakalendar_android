package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class AiredPropDetail(
    @SerializedName("day") val day: Int?,
    @SerializedName("month") val month: Int?,
    @SerializedName("year") val year: Int?
)
