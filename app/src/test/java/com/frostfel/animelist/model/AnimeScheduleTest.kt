package com.frostfel.animelist.model

import com.frostfel.animelist.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

class AnimeScheduleTest {
    private val jst = ZoneId.of("Asia/Tokyo")
    private val madrid = ZoneId.of("Europe/Madrid")

    // Friday 2026-10-09
    private fun jst(day: Int, hour: Int, minute: Int = 0) =
        ZonedDateTime.of(2026, 10, day, hour, minute, 0, 0, jst)

    private val fridays2300 = Broadcast("Fridays", "23:00", "Asia/Tokyo", "Fridays at 23:00 (JST)")

    @Test
    fun laterTheSameDay() {
        val now = ZonedDateTime.of(2026, 10, 9, 10, 0, 0, 0, madrid) // 17:00 JST
        assertEquals(jst(9, 23), fridays2300.nextAiringAfter(now))
    }

    @Test
    fun alreadyAiredMovesToNextWeek() {
        assertEquals(jst(16, 23), fridays2300.nextAiringAfter(jst(9, 23, 30)))
    }

    @Test
    fun minutesAreTakenIntoAccount() {
        val broadcast = fridays2300.copy(time = "23:30")
        assertEquals(jst(9, 23, 30), broadcast.nextAiringAfter(jst(9, 23, 10)))
    }

    @Test
    fun laterInTheWeek() {
        assertEquals(jst(9, 23), fridays2300.nextAiringAfter(jst(6, 12)))
    }

    @Test
    fun lateNightSlotCrossesMidnightForEurope() {
        val saturdays0105 = Broadcast("Saturdays", "01:05", "Asia/Tokyo", null)
        val now = ZonedDateTime.of(2026, 10, 9, 10, 0, 0, 0, madrid)
        val next = saturdays0105.nextAiringAfter(now)!!
        assertEquals(ZonedDateTime.of(2026, 10, 9, 18, 5, 0, 0, madrid).toInstant(), next.toInstant())
    }

    @Test
    fun unknownScheduleHasNoNextEpisode() {
        assertNull(Broadcast(null, null, null, null).nextAiringAfter(jst(9, 12)))
        assertNull(Broadcast("Fridays", "not a time", null, null).nextAiringAfter(jst(9, 12)))
    }

    @Test
    fun missingTimezoneDefaultsToJapan() {
        assertEquals(jst(9, 23), fridays2300.copy(timeZone = null).nextAiringAfter(jst(9, 12)))
    }

    @Test
    fun notBeforeTheFirstEpisode() {
        val anime = Fixtures.airing.copy(
            broadcast = fridays2300,
            status = "Not yet aired",
            aired = Aired("2026-10-23T00:00:00+00:00", null, AiredProp(AiredPropDetail(23, 10, 2026), null, null))
        )
        assertEquals(jst(23, 23), anime.nextEpisodeAt(jst(9, 12)))
    }

    @Test
    fun finishedSeriesHaveNoNextEpisode() {
        val anime = Fixtures.airing.copy(broadcast = fridays2300, status = "Finished Airing")
        assertNull(anime.nextEpisodeAt(jst(9, 12)))
    }

    @Test
    fun nextEpisodeWithinWindow() {
        val anime = Fixtures.airing.copy(broadcast = fridays2300)
        assertEquals(jst(9, 23), anime.nextEpisodeWithin(jst(9, 12), Duration.ofHours(24)))
        assertNull(anime.nextEpisodeWithin(jst(7, 12), Duration.ofHours(24)))
    }
}
