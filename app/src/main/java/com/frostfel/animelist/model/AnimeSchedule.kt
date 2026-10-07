package com.frostfel.animelist.model

import android.content.Context
import com.frostfel.animelist.R
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

private const val STATUS_FINISHED = "Finished Airing"

/** When the next episode airs, or null if it is unknown or the series has finished. */
fun Anime.nextEpisodeAt(now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime? {
    if (status == STATUS_FINISHED) return null
    return broadcast.nextAiringAfter(now, notBefore = startDate())
}

/** Next episode time if it airs within [window] from [now]. */
fun Anime.nextEpisodeWithin(now: ZonedDateTime, window: Duration): ZonedDateTime? =
    nextEpisodeAt(now)?.takeIf { Duration.between(now, it) <= window }

private fun Anime.startDate(): LocalDate? {
    val from = aired.prop?.from ?: return null
    val (year, month, day) = Triple(from.year ?: return null, from.month ?: return null, from.day ?: return null)
    return runCatching { LocalDate.of(year, month, day) }.getOrNull()
}

fun Anime.getNextBroadcastString(context: Context, now: ZonedDateTime = ZonedDateTime.now()): String {
    val next = nextEpisodeAt(now) ?: return broadcast.stringValue ?: ""
    val untilNext = Duration.between(now, next)
    val days = untilNext.toDays()
    val hours = untilNext.toHours() % 24
    val minutes = untilNext.toMinutes() % 60
    val resources = context.resources
    val daysText = resources.getQuantityString(R.plurals.airing_days, days.toInt(), days.toInt())
    val hoursText = resources.getQuantityString(R.plurals.airing_hours, hours.toInt(), hours.toInt())
    val minutesText = resources.getQuantityString(R.plurals.airing_minutes, minutes.toInt(), minutes.toInt())
    return when {
        days > 0 -> context.getString(R.string.two_values_airing, daysText, hoursText)
        hours > 0 -> context.getString(R.string.two_values_airing, hoursText, minutesText)
        else -> context.getString(R.string.one_value_airing, minutesText)
    }
}
