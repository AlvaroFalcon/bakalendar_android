package com.frostfel.animelist.views.season_list.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.frostfel.animelist.data.AnimeDbRepository
import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.views.season_list.paging.AnimePagingSource
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices,
    private val animeDbRepository: AnimeDbRepository
) : AnimeRepository {
    override fun getAnimeList(isFav: Boolean): LiveData<PagingData<Anime>> {
        return if (isFav) {
            return MutableLiveData(PagingData.from(animeDbRepository.getAllNoLive()))
        } else {
            Pager(
                config = PagingConfig(
                    pageSize = PAGE_SIZE,
                    enablePlaceholders = false,
                    initialLoadSize = 2
                ),
                pagingSourceFactory = { AnimePagingSource(apiServices) },
                initialKey = 1
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