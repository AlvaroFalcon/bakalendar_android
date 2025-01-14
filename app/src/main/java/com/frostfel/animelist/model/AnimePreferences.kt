package com.frostfel.animelist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_anime_preferences")
data class AnimePreferences(
    @PrimaryKey(autoGenerate = false)
    val malId: Int,
    val starred: Boolean = false,
    val starredAt: Long? = null,
)