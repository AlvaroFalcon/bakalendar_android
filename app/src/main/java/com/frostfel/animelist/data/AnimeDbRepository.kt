package com.frostfel.animelist.data

import com.frostfel.animelist.model.Anime

interface AnimeDbRepository {
    suspend fun addAnime(anime: Anime)
    suspend fun removeAnime(anime: Anime)
    suspend fun addOrRemove(anime: Anime)
}