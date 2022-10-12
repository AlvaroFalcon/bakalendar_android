package com.frostfel.animelist.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Aired(
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String?,
    @SerializedName("prop") val prop: AiredProp?
) : Parcelable
