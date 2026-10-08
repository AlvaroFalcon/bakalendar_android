package com.frostfel.animelist.data.repository

import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.data.dao.AnimeDao
import com.frostfel.animelist.model.EpisodeDto
import com.frostfel.animelist.model.RelatedItem
import com.frostfel.animelist.model.StreamingLink
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
    // Kept in memory for the session, so reopening a detail does not refetch.
    private val cache = mutableMapOf<String, Any>()
    private val mutex = Mutex()

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Any> cached(key: String, load: suspend () -> T): T {
        mutex.withLock { cache[key] }?.let { return it as T }
        val value = load()
        mutex.withLock { cache[key] = value }
        return value
    }

    /** Related anime and manga. */
    suspend fun getRelations(malId: Int): List<RelatedItem> =
        cached("relations/$malId") { apiServices.getAnimeRelations(malId).toRelatedItems() }

    /** "You might also like": empty for most new shows (needs MyAnimeList user votes). */
    suspend fun getRecommendations(malId: Int): List<RelatedItem> =
        cached("recommendations/$malId") { apiServices.getAnimeRecommendations(malId).toRelatedItems() }

    /**
     * Aired episodes. Long-running series have several pages of 100: the latest page is the
     * relevant one, so that one is shown.
     */
    suspend fun getEpisodes(malId: Int): List<EpisodeDto> = cached("episodes/$malId") {
        val first = apiServices.getAnimeEpisodes(malId)
        val lastPage = first.pagination?.lastVisiblePage ?: 1
        val page = if (first.pagination?.hasNextPage == true && lastPage > 1) {
            apiServices.getAnimeEpisodes(malId, lastPage)
        } else first
        page.data.orEmpty()
    }

    /** Where the series can be watched. */
    suspend fun getStreaming(malId: Int): List<StreamingLink> = cached("streaming/$malId") {
        apiServices.getAnimeStreaming(malId).data.orEmpty().filter { !it.name.isNullOrBlank() && !it.url.isNullOrBlank() }
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
