package com.frostfel.animelist.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnimeWithPreferences(
    @Embedded val anime: Anime,

    @Relation(
        parentColumn = "malId",
        entityColumn = "malId"
    )
    val userPreferences: AnimePreferences?
) : Parcelable