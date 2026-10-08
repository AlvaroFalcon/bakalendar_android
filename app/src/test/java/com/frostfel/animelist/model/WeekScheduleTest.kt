package com.frostfel.animelist.model

import com.frostfel.animelist.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

class WeekScheduleTest {
    private val madrid = ZoneId.of("Europe/Madrid")
    private val now = ZonedDateTime.of(2026, 10, 7, 12, 0, 0, 0, madrid) // Wednesday

    private fun item(id: Int, day: String, time: String, status: String = "Currently Airing") =
        AnimeWithPreferences(
            Fixtures.anime(id, "Anime $id").copy(broadcast = Broadcast(day, time, "Asia/Tokyo", null), status = status),
            null
        )

    @Test
    fun groupsByLocalWeekdaySortedByTime() {
        val week = listOf(
            item(1, "Fridays", "23:00"),   // Friday 16:00 in Madrid
            item(2, "Saturdays", "01:05"), // Friday 18:05 in Madrid
            item(3, "Fridays", "09:00"),   // Friday 02:00 in Madrid
            item(4, "Mondays", "22:00"),   // Monday 15:00
        ).weekSchedule(now)

        assertEquals(listOf(3, 1, 2), week.getValue(DayOfWeek.FRIDAY).map { it.item.anime.malId })
        assertEquals(listOf(4), week.getValue(DayOfWeek.MONDAY).map { it.item.anime.malId })
        assertEquals(emptyList<CalendarEntry>(), week.getValue(DayOfWeek.SATURDAY))
        assertEquals(7, week.size)
        assertEquals(madrid, week.getValue(DayOfWeek.FRIDAY).first().airsAt.zone)
    }

    @Test
    fun leavesOutUnknownAndFinishedSchedules() {
        val week = listOf(
            item(1, "Fridays", "23:00", status = "Finished Airing"),
            AnimeWithPreferences(Fixtures.anime(2).copy(broadcast = Broadcast(null, null, null, null)), null),
        ).weekSchedule(now)

        assertEquals(0, week.values.sumOf { it.size })
    }
}
