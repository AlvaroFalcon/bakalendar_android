package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName

data class Pagination(
    @SerializedName("last_visible_page") val lastVisiblePage: Int,
    @SerializedName("has_next_page") val hasNextPage: Boolean,
    @SerializedName("items") val currentPage: PaginationItems,
)
