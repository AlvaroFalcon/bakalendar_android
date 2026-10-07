package com.frostfel.animelist.data.mediators

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.frostfel.animelist.data.season.SeasonRefresher
import com.frostfel.animelist.model.AnimeWithPreferences
import kotlinx.coroutines.CancellationException

/**
 * Room is the single source of truth. The season is small, so a refresh downloads all of it
 * and there is never anything to append or prepend.
 */
@OptIn(ExperimentalPagingApi::class)
class SeasonRemoteMediator(
    private val seasonRefresher: SeasonRefresher,
) : RemoteMediator<Int, AnimeWithPreferences>() {

    override suspend fun initialize(): InitializeAction =
        if (seasonRefresher.needsRefresh()) {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        } else {
            InitializeAction.SKIP_INITIAL_REFRESH
        }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, AnimeWithPreferences>
    ): MediatorResult {
        if (loadType != LoadType.REFRESH) {
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        return try {
            seasonRefresher.refreshSeason()
            MediatorResult.Success(endOfPaginationReached = true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
