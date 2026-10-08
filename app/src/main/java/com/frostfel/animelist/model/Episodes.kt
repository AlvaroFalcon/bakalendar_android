package com.frostfel.animelist.model

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime
import java.time.ZonedDateTime

/** `GET anime/{id}/episodes`: only episodes that already aired, 100 per page. */
data class EpisodesResponse(
    @SerializedName("data") val data: List<EpisodeDto>?,
    @SerializedName("pagination") val pagination: EpisodesPagination?
)

data class EpisodesPagination(
    @SerializedName("last_visible_page") val lastVisiblePage: Int?,
    @SerializedName("has_next_page") val hasNextPage: Boolean?
)

data class EpisodeDto(
    @SerializedName("mal_id") val number: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("aired") val aired: String?,
    @SerializedName("filler") val filler: Boolean?,
    @SerializedName("recap") val recap: Boolean?,
    @SerializedName("url") val url: String?
)

/** `GET anime/{id}/streaming`: platforms the series is on (not per episode or region). */
data class StreamingResponse(
    @SerializedName("data") val data: List<StreamingLink>?
)

data class StreamingLink(
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)

/** One row of the episodes tab. [date] is the air date, or the expected one when not [aired]. */
data class EpisodeItem(
    val number: Int,
    val title: String?,
    val date: ZonedDateTime?,
    val aired: Boolean,
    val filler: Boolean = false,
    val recap: Boolean = false,
    val url: String? = null,
)

/**
 * Aired episodes from the API plus the ones still to come, projected weekly from the next
 * broadcast up to the announced episode count (or just the next one when the count is unknown).
 */
fun List<EpisodeDto>.withUpcoming(anime: Anime?, now: ZonedDateTime = ZonedDateTime.now()): List<EpisodeItem> {
    val aired = sortedBy { it.number }.map { episode ->
        EpisodeItem(
            number = episode.number,
            title = episode.title,
            date = episode.aired?.let { runCatching { OffsetDateTime.parse(it).toZonedDateTime() }.getOrNull() },
            aired = true,
            filler = episode.filler == true,
            recap = episode.recap == true,
            url = episode.url,
        )
    }
    if (anime == null) return aired
    val next = anime.nextEpisodeAt(now) ?: return aired
    val lastAired = aired.maxOfOrNull { it.number } ?: 0
    val lastExpected = if (anime.episodes > lastAired) anime.episodes else lastAired + 1
    val upcoming = (lastAired + 1..lastExpected).map { number ->
        EpisodeItem(
            number = number,
            title = null,
            date = next.plusWeeks((number - lastAired - 1).toLong()),
            aired = false,
        )
    }
    return aired + upcoming
}
