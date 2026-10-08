package com.frostfel.animelist.views.season_list.repository

import com.frostfel.animelist.model.AnimeWithPreferences
import kotlinx.coroutines.flow.Flow

interface AnimeRepository {
    fun getAnimeList(isFav: Boolean): Flow<List<AnimeWithPreferences>>

    /** Genres present in the list, most common first. */
    fun getGenres(isFav: Boolean): Flow<List<String>>
}
