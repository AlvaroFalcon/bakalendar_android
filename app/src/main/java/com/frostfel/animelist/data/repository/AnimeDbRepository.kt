package com.frostfel.animelist.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeWithPreferences

interface AnimeDbRepository {
    suspend fun addAnime(anime: Anime)
    suspend fun addAllAnime(animes: List<Anime>)
    suspend fun removeAnime(anime: Anime)
    suspend fun addOrRemove(anime: Anime)
    fun getById(id: Int): LiveData<List<AnimeWithPreferences>>
    fun getAll(): LiveData<List<AnimeWithPreferences>>
    fun pagingSource() : PagingSource<Int, AnimeWithPreferences>
    fun getAllNoLive(): List<AnimeWithPreferences>
    fun getAllFav(): LiveData<List<AnimeWithPreferences>>
    fun getAllFavNoLive(): List<AnimeWithPreferences>
    fun setStarred(malId: Int, starred: Boolean)
}