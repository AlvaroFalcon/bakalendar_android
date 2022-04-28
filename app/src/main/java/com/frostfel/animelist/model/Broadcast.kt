package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class Broadcast(
    @SerializedName("day") val day: String,
    @SerializedName("time") val time: String,
    @SerializedName("timezone") val timeZone: String,
    @SerializedName("string") val stringValue: String,
)
