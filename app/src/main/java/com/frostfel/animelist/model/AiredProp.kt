package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class AiredProp(
    @SerializedName("from") val from: AiredPropDetail,
    @SerializedName("to") val to: AiredPropDetail,
    @SerializedName("string") val stringValue: String
)
