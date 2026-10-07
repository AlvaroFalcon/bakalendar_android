package com.frostfel.animelist.views.season_list.repository

import androidx.paging.PagingData
import com.frostfel.animelist.model.AnimeWithPreferences
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    fun getAnimeList(isFav: Boolean): Flow<PagingData<AnimeWithPreferences>>
}
