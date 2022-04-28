package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

open class Images(
    @SerializedName("jpg") val jpg: Image,
    @SerializedName("webp") val webp: Image
)


