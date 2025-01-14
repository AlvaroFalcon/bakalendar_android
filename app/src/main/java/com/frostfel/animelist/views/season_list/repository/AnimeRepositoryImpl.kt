package com.frostfel.animelist.views.season_list.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.data.mediators.AnimeRemoteMediator
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeWithPreferences
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices,
    private val animeDbRepository: AnimeDbRepository,
    private val appDatabase: AppDatabase
) : AnimeRepository {
    @OptIn(ExperimentalPagingApi::class)
    override fun getAnimeList(isFav: Boolean): LiveData<PagingData<AnimeWithPreferences>> {
        return if (isFav) {
            val favLiveData = animeDbRepository.getAllFav()
            Transformations.map(favLiveData) { list ->
                PagingData.from(list)
            }
        } else {
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    prefetchDistance = 10,
                    initialLoadSize = PAGE_SIZE
                ),
                remoteMediator =  AnimeRemoteMediator(appDatabase,apiServices),
                pagingSourceFactory = { animeDbRepository.pagingSource() },

            ).liveData
        }

    }

    override suspend fun getAnimeById(id: Int): Anime {
        return apiServices.getAnimeById(id).data
    }

    companion object {
        const val PAGE_SIZE = 25
    }
}