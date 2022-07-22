package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class PaginationItems(
    @SerializedName("count") val count: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("per_page") val perPage: Int,
)
