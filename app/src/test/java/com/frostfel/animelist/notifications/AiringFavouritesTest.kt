package com.frostfel.animelist.notifications

import com.frostfel.animelist.model.AnimePreferences
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.Broadcast
import com.frostfel.animelist.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class AiringFavouritesTest {
    private val jst = ZoneId.of("Asia/Tokyo")
    private val now = ZonedDateTime.of(2026, 10, 9, 12, 0, 0, 0, jst) // Friday

    private fun favourite(id: Int, day: String?, time: String?) = AnimeWithPreferences(
        Fixtures.anime(id).copy(broadcast = Broadcast(day, time, "Asia/Tokyo", null)),
        AnimePreferences(id, true, 0L)
    )

    @Test
    fun onlyFavouritesInTheNext24HoursSoonestFirst() {
        val favourites = listOf(
            favourite(1, "Saturdays", "01:05"),
            favourite(2, "Fridays", "23:00"),
            favourite(3, "Mondays", "22:00"),
            favourite(4, null, null),
        )
        assertEquals(listOf(2, 1), favourites.airingWithin(now).map { it.anime.malId })
    }

    @Test
    fun delayUntilTheNextNotificationTime() {
        val at = LocalTime.of(10, 0)
        assertEquals(
            Duration.ofHours(2),
            FavouritesNotificationScheduler.delayUntilNext(at, now.withHour(8))
        )
        assertEquals(
            Duration.ofHours(22),
            FavouritesNotificationScheduler.delayUntilNext(at, now)
        )
    }

    @Test
    fun formatsTimeInTheUserTimezone() {
        val madrid = ZoneId.of("Europe/Madrid")
        val today = ZonedDateTime.now(madrid).with(LocalTime.of(0, 1))
        assertEquals("00:01", FavouritesNotifier.formatAiringTime(today, madrid, Locale.UK))
        val later = today.plusDays(2).withHour(18).withMinute(5)
        val expected = later.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.UK) + " 18:05"
        assertEquals(expected, FavouritesNotifier.formatAiringTime(later, madrid, Locale.UK))
    }
}
