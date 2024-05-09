package com.frostfel.animelist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RemoteKey(
    @PrimaryKey val id: String,
    val nextPage: Int,
)