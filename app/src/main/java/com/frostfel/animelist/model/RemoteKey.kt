package com.frostfel.animelist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RemoteKey(
    @PrimaryKey(autoGenerate = false) val id: String,
    val prevKey: Int?,
    val nextKey: Int?,
    val currentPage: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val animeId: Int
)