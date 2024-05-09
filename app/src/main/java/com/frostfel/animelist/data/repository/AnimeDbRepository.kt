package com.frostfel.animelist.data.repository

import androidx.lifecycle.LiveData
import com.frostfel.animelist.model.Anime

interface AnimeDbRepository {
    suspend fun addAnime(anime: Anime)
    suspend fun removeAnime(anime: Anime)
    suspend fun addOrRemove(anime: Anime)
    fun getById(id: Int): LiveData<List<Anime>>
    fun getAll(): LiveData<List<Anime>>
    fun getAllNoLive(): List<Anime>
}