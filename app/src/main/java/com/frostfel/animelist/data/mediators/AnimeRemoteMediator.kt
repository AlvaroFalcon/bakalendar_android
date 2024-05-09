
package com.frostfel.animelist.data.mediators

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.data.repository.RemoteKeyRepository
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.RemoteKey
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class AnimeRemoteMediator @Inject constructor(
    private val animeRepository: AnimeDbRepository,
    private val remoteKeyRepository: RemoteKeyRepository,
    private val apiServices: ApiServices,
) : RemoteMediator<Int, Anime>() {

    private val REMOTE_KEY_ID = "pokemon"

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Anime>,
    ): MediatorResult {
        return try {
            val currentPage = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    // RETRIEVE NEXT OFFSET FROM DATABASE
                    val remoteKey = remoteKeyRepository.getById(REMOTE_KEY_ID)
                    if (remoteKey.nextPage == 0) // END OF PAGINATION REACHED
                        return MediatorResult.Success(endOfPaginationReached = true)
                    remoteKey.nextPage
                }
            }
            // MAKE API CALL
            val apiResponse = apiServices.getCurrentSeason(currentPage)

            val results = apiResponse.data
            val nextPage: Int = if (apiResponse.pagination.hasNextPage) currentPage + state.config.pageSize else 0
            // SAVE RESULTS AND NEXT OFFSET TO DATABASE
                if (loadType == LoadType.REFRESH) {
                    // IF REFRESHING, CLEAR DATABASE FIRST
                    //appDatabase.animeDao().deleteAll()
                    remoteKeyRepository.deleteById(REMOTE_KEY_ID)
                }
                animeRepository.addAnime(
                    results.first()
                )
                remoteKeyRepository.insert(
                    RemoteKey(
                        id = REMOTE_KEY_ID,
                        nextPage = nextPage,
                    )
                )
            // CHECK IF END OF PAGINATION REACHED
            MediatorResult.Success(endOfPaginationReached = results.size < state.config.pageSize)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}