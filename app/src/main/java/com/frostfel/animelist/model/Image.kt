package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("small_image_url") val smallImageUrl: String,
    @SerializedName("large_image_url") val largeImageUrl: String
)