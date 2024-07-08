
package com.frostfel.animelist.data.mediators

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.RemoteKey
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class AnimeRemoteMediator @Inject constructor(
    private val appDatabase: AppDatabase,
    private val apiServices: ApiServices,
) : RemoteMediator<Int, Anime>() {

    override suspend fun initialize(): InitializeAction {
        val twelveHoursAgo = System.currentTimeMillis() - 12 * 60 * 60 * 1000
        val lastRefreshTime = appDatabase.remoteKeyDao().getCreationTime() ?: 0L

        return if (lastRefreshTime < twelveHoursAgo) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }
    }


    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Anime>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextKey?.minus(1) ?: 1
                }
                LoadType.PREPEND -> {
                    val remoteKeys = getRemoteKeyForFirstItem(state)
                    val prevKey = remoteKeys?.prevKey
                    prevKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                    nextKey ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
            }

            val response = apiServices.getCurrentSeason(page)
            val nextPage = if(response.pagination.hasNextPage) page.plus(1) else -1
            val endOfPaginationReached = nextPage == -1
            appDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    appDatabase.remoteKeyDao().clearAll()
                    appDatabase.animeDao().clearAll()
                }
                // Update RemoteKey for this query.
                val prevKey = if(page > 1) page - 1 else null
                val nextKey = if(endOfPaginationReached) null else page + 1
                val remoteKeys = response.data.map {
                    RemoteKey(id = it.malId.toString(), prevKey = prevKey,nextKey = nextKey, currentPage = page, animeId = it.malId)
                }
                appDatabase.remoteKeyDao().insertAll(remoteKeys)
                appDatabase.animeDao().insertAll(response.data.map { it.copy(page = page)})
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Anime>): RemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.malId?.let { id ->
                appDatabase.remoteKeyDao().getByAnimeId(id)
            }
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Anime>): RemoteKey? {
        return state.pages.firstOrNull {
            it.data.isNotEmpty()
        }?.data?.firstOrNull()?.let { anime ->
            appDatabase.remoteKeyDao().getByAnimeId(anime.malId)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Anime>): RemoteKey? {
        return state.pages.lastOrNull {
            it.data.isNotEmpty()
        }?.data?.lastOrNull()?.let { anime ->
            appDatabase.remoteKeyDao().getByAnimeId(anime.malId)
        }
    }

}