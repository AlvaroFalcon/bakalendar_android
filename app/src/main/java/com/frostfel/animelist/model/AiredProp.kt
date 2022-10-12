package com.frostfel.animelist.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AiredProp(
    @SerializedName("from") val from: AiredPropDetail?,
    @SerializedName("to") val to: AiredPropDetail?,
    @SerializedName("string") val stringValue: String?
) : Parcelable
