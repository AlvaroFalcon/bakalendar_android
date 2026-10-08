package com.frostfel.animelist.model

/** YouTube thumbnail (hqdefault always exists, unlike maxresdefault). */
val TrailerInfo.thumbnailUrl: String?
    get() = youtubeId?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

val TrailerInfo.watchUrl: String?
    get() = youtubeId?.let { "https://www.youtube.com/watch?v=$it" }
