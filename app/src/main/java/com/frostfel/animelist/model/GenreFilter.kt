package com.frostfel.animelist.model

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
