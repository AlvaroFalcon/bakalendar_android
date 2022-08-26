package com.frostfel.animelist.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
open class Images(
    @SerializedName("jpg") val jpg: Image,
    @SerializedName("webp") val webp: Image
) : Parcelable


