package com.frostfel.animelist.views.season_list.repository

import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.genresByPopularity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val animeDbRepository: AnimeDbRepository,
) : AnimeRepository {

    override fun getAnimeList(isFav: Boolean): Flow<List<AnimeWithPreferences>> {
        return if (isFav) {
            animeDbRepository.getAllFavFlow()
        } else {
            animeDbRepository.seasonWithPreferencesFlow()
        }
    }

    override fun getGenres(isFav: Boolean): Flow<List<String>> {
        val animes = if (isFav) {
            animeDbRepository.getAllFavFlow().map { list -> list.map { it.anime } }
        } else {
            animeDbRepository.seasonFlow()
        }
        return animes.map { it.genresByPopularity() }.distinctUntilChanged()
    }
}
