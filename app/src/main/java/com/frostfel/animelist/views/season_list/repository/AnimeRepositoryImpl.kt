package com.frostfel.animelist.views.season_list.repository

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.views.season_list.paging.AnimePagingSource
import javax.inject.Inject

class AnimeRepositoryImpl @Inject constructor(
    private val apiServices: ApiServices
) : AnimeRepository {
    override fun getAnimeList(): LiveData<PagingData<Anime>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = 2
            ),
            pagingSourceFactory = { AnimePagingSource(apiServices) },
            initialKey = 1
        ).liveData
    }

    companion object {
        const val PAGE_SIZE = 25
    }
}