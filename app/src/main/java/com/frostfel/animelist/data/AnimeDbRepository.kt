package com.frostfel.animelist.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frostfel.animelist.model.Anime

interface AnimeDbRepository {
    suspend fun addAnime(anime: Anime)
    suspend fun removeAnime(anime: Anime)
    suspend fun addOrRemove(anime: Anime)
    fun getAll(): LiveData<List<Anime>>
}