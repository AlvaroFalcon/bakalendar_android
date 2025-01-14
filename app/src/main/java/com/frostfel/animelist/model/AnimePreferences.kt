package com.frostfel.animelist.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_anime_preferences",
    foreignKeys = [
        ForeignKey(
            entity = Anime::class,
            parentColumns = ["malId"],
            childColumns = ["malId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class AnimePreferences(
    @PrimaryKey(autoGenerate = false)
    val malId: Int,
    @ColumnInfo(defaultValue = "0")
    val starred: Boolean = false,
    val starredAt: Long? = null,
)