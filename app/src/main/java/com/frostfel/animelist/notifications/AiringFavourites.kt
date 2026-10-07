package com.frostfel.animelist.notifications

import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.nextEpisodeWithin
import java.time.Duration
import java.time.ZonedDateTime

data class UpcomingEpisode(val anime: Anime, val airsAt: ZonedDateTime)

/** Favourites with an episode in the next [window], soonest first. */
fun List<AnimeWithPreferences>.airingWithin(
    now: ZonedDateTime,
    window: Duration = Duration.ofHours(24)
): List<UpcomingEpisode> =
    mapNotNull { favourite ->
        favourite.anime.nextEpisodeWithin(now, window)?.let { UpcomingEpisode(favourite.anime, it) }
    }.sortedBy { it.airsAt }
