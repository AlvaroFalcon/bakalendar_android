package com.frostfel.animelist.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrailerInfo(
    @SerializedName("youtube_id") val youtubeId: String,
    @SerializedName("url") val url: String,
    @SerializedName("embed_url") val embedUrl: String
) : Parcelable
