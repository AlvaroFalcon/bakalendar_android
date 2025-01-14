package com.frostfel.animelist.model

import androidx.room.Embedded
import androidx.room.Relation

data class AnimeWithPreferences(
    @Embedded val anime: Anime,

    @Relation(
        parentColumn = "malId",
        entityColumn = "malId"
    )
    val userPreferences: AnimePreferences?
)