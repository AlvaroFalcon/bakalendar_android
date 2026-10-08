package com.frostfel.animelist.data.repository

import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.data.dao.AnimeDao
import com.frostfel.animelist.model.RelatedItem
import com.frostfel.animelist.model.toRelatedItems
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimeDetailRepository @Inject constructor(
    private val apiServices: ApiServices,
    private val animeDao: AnimeDao,
) {
    private val relationsCache = mutableMapOf<Int, List<RelatedItem>>()
    private val mutex = Mutex()

    /** Related anime and manga; kept in memory so reopening a detail does not refetch. */
    suspend fun getRelations(malId: Int): List<RelatedItem> {
        mutex.withLock { relationsCache[malId] }?.let { return it }
        val items = apiServices.getAnimeRelations(malId).toRelatedItems()
        mutex.withLock { relationsCache[malId] = items }
        return items
    }

    /**
     * Makes sure the anime is in the database (e.g. a related one from another season).
     * It is stored outside the season (page 0), so the next refresh cleans it up unless it
     * becomes a favourite.
     */
    suspend fun ensureCached(malId: Int) {
        if (animeDao.exists(malId)) return
        val anime = apiServices.getAnimeById(malId).data
        animeDao.insertAll(listOf(anime.copy(page = 0)))
    }
}
