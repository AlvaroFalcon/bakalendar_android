package com.frostfel.animelist.model

import java.time.ZonedDateTime

/** Genre names, most common first (ties alphabetically). */
fun List<Anime>.genresByPopularity(): List<String> =
    flatMap { anime -> anime.genres.map { it.name } }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .map { it.key }

/** Matches the title text and any of [selectedGenres] (no genres selected = all). */
fun Anime.matches(query: String, selectedGenres: Set<String>): Boolean {
    val text = query.trim()
    val matchesText = text.isEmpty() || title?.contains(text, ignoreCase = true) == true
    val matchesGenre = selectedGenres.isEmpty() || genres.any { it.name in selectedGenres }
    return matchesText && matchesGenre
}

enum class SortOrder {
    /** As downloaded: the season's new shows by popularity, then the continuing ones. */
    POPULARITY,

    /** Soonest next episode first; anime without a known next episode go last. */
    NEXT_EPISODE,
}

fun List<AnimeWithPreferences>.sortedBy(order: SortOrder, now: ZonedDateTime): List<AnimeWithPreferences> =
    when (order) {
        SortOrder.POPULARITY -> this
        // sortedBy is stable, so ties keep the popularity order.
        SortOrder.NEXT_EPISODE -> map { it to it.anime.nextEpisodeAt(now)?.toInstant() }
            .sortedWith(compareBy(nullsLast()) { it.second })
            .map { it.first }
    }
