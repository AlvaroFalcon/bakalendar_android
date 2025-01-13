package com.frostfel.animelist.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

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
    @SerializedName ("mal_id") val malId: Int,
    val starred: Boolean = false,
    val starredAt: Long? = null,
)