package com.frostfel.animelist.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "user_anime_preferences")
@Parcelize
data class AnimePreferences(
    @PrimaryKey(autoGenerate = false)
    val malId: Int,
    val starred: Boolean = false,
    val starredAt: Long? = null,
) : Parcelable