package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class TrailerInfo(
    @SerializedName("youtube_id") val youtubeId: String,
    @SerializedName("url") val url: String,
    @SerializedName("embed_url") val embedUrl: String
)
