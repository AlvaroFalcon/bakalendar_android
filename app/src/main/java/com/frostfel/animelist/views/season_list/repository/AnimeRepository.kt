package com.frostfel.animelist.views.season_list.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.frostfel.animelist.model.Anime

interface AnimeRepository {
    fun getAnimeList(isFav: Boolean): LiveData<PagingData<Anime>>
}