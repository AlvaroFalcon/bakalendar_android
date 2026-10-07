package com.frostfel.animelist.data.season

import androidx.room.withTransaction
import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.model.Anime
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the local copy of the current season in sync with the API.
 *
 * The whole season (a handful of pages) is downloaded before touching the database, so a
 * failed refresh never leaves a half-empty cache and the list is always complete offline.
 */
@Singleton
class SeasonRefresher @Inject constructor(
    private val apiServices: ApiServices,
    private val appDatabase: AppDatabase,
    private val cacheStore: SeasonCacheStore,
) {
    internal var clock: () -> Long = System::currentTimeMillis

    private val animeDao get() = appDatabase.animeDao()

    suspend fun needsRefresh(): Boolean =
        animeDao.countSeason() == 0 || clock() - cacheStore.lastRefreshMillis > MAX_AGE_MILLIS

    /** Throws (IOException, HttpException…) if the season could not be downloaded. */
    suspend fun refreshSeason() {
        val season = fetchWholeSeason()
        val seasonIds = season.map { it.malId }
        appDatabase.withTransaction {
            animeDao.deleteOutsideSeasonExceptFavourites(seasonIds)
            animeDao.markOutsideSeason(seasonIds)
            animeDao.insertAll(season.mapIndexed { index, anime -> anime.copy(page = index + 1) })
        }
        cacheStore.lastRefreshMillis = clock()
        refreshFavouritesOutsideSeason()
    }

    /**
     * Favourites from previous seasons that are still airing are not in `seasons/now`;
     * update them one by one so their schedule (and airing status) stays current.
     */
    suspend fun refreshFavouritesOutsideSeason() {
        animeDao.getAiringFavouritesOutsideSeason().forEach { favourite ->
            try {
                val anime = apiServices.getAnimeById(favourite.malId).data
                animeDao.insertAll(listOf(anime.copy(page = 0)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Could not refresh favourite ${favourite.malId}")
            }
        }
    }

    private suspend fun fetchWholeSeason(): List<Anime> {
        val season = LinkedHashMap<Int, Anime>()
        var page = 1
        do {
            val response = apiServices.getCurrentSeason(page, PAGE_SIZE)
            response.data.forEach { season.putIfAbsent(it.malId, it) }
            page++
        } while (response.pagination.hasNextPage && page <= MAX_PAGES)
        return season.values.toList()
    }

    companion object {
        const val MAX_AGE_MILLIS = 12 * 60 * 60 * 1000L
        private const val PAGE_SIZE = 50
        private const val MAX_PAGES = 20
    }
}
