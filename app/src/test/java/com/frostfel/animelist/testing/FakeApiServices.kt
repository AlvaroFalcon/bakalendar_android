package com.frostfel.animelist.testing

import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeResponse
import com.frostfel.animelist.model.Pagination
import com.frostfel.animelist.model.PaginationItems
import com.frostfel.animelist.model.EpisodeDto
import com.frostfel.animelist.model.EpisodesPagination
import com.frostfel.animelist.model.EpisodesResponse
import com.frostfel.animelist.model.RecommendationsResponse
import com.frostfel.animelist.model.RelationsResponse
import com.frostfel.animelist.model.StreamingLink
import com.frostfel.animelist.model.StreamingResponse
import com.frostfel.animelist.model.SeasonResponse
import java.io.IOException

class FakeApiServices : ApiServices {
    var season: List<Anime> = emptyList()
    var byId: Map<Int, Anime> = emptyMap()
    var relations: Map<Int, RelationsResponse> = emptyMap()
    var recommendations: Map<Int, RecommendationsResponse> = emptyMap()
    var episodes: Map<Int, List<EpisodeDto>> = emptyMap()
    var streaming: Map<Int, List<StreamingLink>> = emptyMap()
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

    override suspend fun getAnimeRecommendations(id: Int): RecommendationsResponse =
        recommendations[id] ?: RecommendationsResponse(emptyList())

    override suspend fun getAnimeEpisodes(id: Int, page: Int): EpisodesResponse =
        EpisodesResponse(episodes[id] ?: throw IOException("offline"), EpisodesPagination(1, false))

    override suspend fun getAnimeStreaming(id: Int): StreamingResponse =
        StreamingResponse(streaming[id].orEmpty())

    override suspend fun getAnimeById(id: Int): AnimeResponse =
        AnimeResponse(byId[id] ?: throw IOException("offline"))
}
