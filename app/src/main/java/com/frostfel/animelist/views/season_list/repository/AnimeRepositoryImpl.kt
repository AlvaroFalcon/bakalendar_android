package com.frostfel.animelist.views.season_list.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.frostfel.animelist.data.mediators.SeasonRemoteMediator
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.data.season.SeasonRefresher
import com.frostfel.animelist.model.AnimeWithPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val animeDbRepository: AnimeDbRepository,
    private val seasonRefresher: SeasonRefresher,
) : AnimeRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getAnimeList(isFav: Boolean): Flow<PagingData<AnimeWithPreferences>> {
        return if (isFav) {
            animeDbRepository.getAllFavFlow().map { PagingData.from(it) }
        } else {
            Pager(
                config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE),
                remoteMediator = SeasonRemoteMediator(seasonRefresher),
                pagingSourceFactory = { animeDbRepository.seasonPagingSource() },
            ).flow
        }
    }

    companion object {
        const val PAGE_SIZE = 25
    }
}
