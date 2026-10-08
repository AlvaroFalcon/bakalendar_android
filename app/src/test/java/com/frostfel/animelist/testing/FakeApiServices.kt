package com.frostfel.animelist.testing

import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeResponse
import com.frostfel.animelist.model.Pagination
import com.frostfel.animelist.model.PaginationItems
import com.frostfel.animelist.model.RelationsResponse
import com.frostfel.animelist.model.SeasonResponse
import java.io.IOException

class FakeApiServices : ApiServices {
    var season: List<Anime> = emptyList()
    var byId: Map<Int, Anime> = emptyMap()
    var relations: Map<Int, RelationsResponse> = emptyMap()
    var failOnPage: Int? = null
    val requestedPages = mutableListOf<Int>()

    override suspend fun getCurrentSeason(page: Int, limit: Int, continuing: Boolean): SeasonResponse {
        requestedPages += page
        if (page == failOnPage) throw IOException("offline")
        val chunks = season.chunked(limit)
        val data = chunks.getOrElse(page - 1) { emptyList() }
        return SeasonResponse(
            data,
            Pagination(chunks.size, page < chunks.size, PaginationItems(data.size, season.size, limit))
        )
    }

    override suspend fun getAnimeRelations(id: Int): RelationsResponse =
        relations[id] ?: RelationsResponse(emptyList())

    override suspend fun getAnimeById(id: Int): AnimeResponse =
        AnimeResponse(byId[id] ?: throw IOException("offline"))
}
