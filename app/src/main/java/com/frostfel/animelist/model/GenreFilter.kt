package com.frostfel.animelist.model

/** Genre names, most common first (ties alphabetically). */
fun List<Anime>.genresByPopularity(): List<String> =
    flatMap { anime -> anime.genres.map { it.name } }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
        .map { it.key }

fun Anime.matches(query: String, genre: String?): Boolean {
    val text = query.trim()
    val matchesText = text.isEmpty() || title?.contains(text, ignoreCase = true) == true
    val matchesGenre = genre == null || genres.any { it.name == genre }
    return matchesText && matchesGenre
}
