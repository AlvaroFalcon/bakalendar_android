package com.frostfel.animelist.model

import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime

data class CalendarEntry(val item: AnimeWithPreferences, val airsAt: ZonedDateTime)

/**
 * Groups anime by the weekday their next episode airs on, in the timezone of [now] (the
 * user's), sorted by time. Only episodes in the next 7 days count, so series that have not
 * started yet or have finished are left out.
 */
fun List<AnimeWithPreferences>.weekSchedule(now: ZonedDateTime): Map<DayOfWeek, List<CalendarEntry>> {
    val byDay = mapNotNull { item ->
        item.anime.nextEpisodeWithin(now, Duration.ofDays(7))
            ?.let { CalendarEntry(item, it.withZoneSameInstant(now.zone)) }
    }.groupBy { it.airsAt.dayOfWeek }
    return DayOfWeek.values().associateWith { day ->
        byDay[day].orEmpty().sortedWith(
            compareBy<CalendarEntry> { it.airsAt.toLocalTime() }.thenBy { it.item.anime.title }
        )
    }
}
