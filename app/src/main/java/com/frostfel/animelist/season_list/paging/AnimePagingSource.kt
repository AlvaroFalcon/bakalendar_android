package com.frostfel.animelist.season_list.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.model.Anime
import java.lang.Exception

class AnimePagingSource(private val apiServices: ApiServices): PagingSource<Int, Anime>() {
    override fun getRefreshKey(state: PagingState<Int, Anime>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Anime> {
        return try {
            val page = params.key ?: 1
            val response = apiServices.getCurrentSeason(page)
            LoadResult.Page(data = response.data, nextKey = page + 1, prevKey = if (page == 1) null else page)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}