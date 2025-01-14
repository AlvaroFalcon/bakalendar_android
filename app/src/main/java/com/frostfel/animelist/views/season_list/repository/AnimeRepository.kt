package com.frostfel.animelist.views.season_list.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeWithPreferences

interface AnimeRepository {
    fun getAnimeList(isFav: Boolean): LiveData<PagingData<AnimeWithPreferences>>
    suspend fun getAnimeById(id: Int): Anime
}