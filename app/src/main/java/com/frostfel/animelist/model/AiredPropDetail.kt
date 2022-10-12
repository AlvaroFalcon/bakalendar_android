package com.frostfel.animelist.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AiredPropDetail(
    @SerializedName("day") val day: Int?,
    @SerializedName("month") val month: Int?,
    @SerializedName("year") val year: Int?
) : Parcelable
