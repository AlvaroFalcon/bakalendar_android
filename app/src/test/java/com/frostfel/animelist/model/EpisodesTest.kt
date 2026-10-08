package com.frostfel.animelist.model

import com.frostfel.animelist.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class EpisodesTest {
    private val jst = ZoneId.of("Asia/Tokyo")
    private val now = ZonedDateTime.of(2026, 10, 8, 12, 0, 0, 0, jst) // Thursday
    private val fridays = Broadcast("Fridays", "23:00", "Asia/Tokyo", null)

    private fun aired(number: Int, filler: Boolean = false) =
        EpisodeDto(number, "Episode title $number", "2026-10-02T00:00:00+00:00", filler, false, "https://mal/ep/$number")

    @Test
    fun projectsTheRemainingEpisodesWeekly() {
        val anime = Fixtures.airing.copy(broadcast = fridays, episodes = 4, status = "Currently Airing")
        val items = listOf(aired(1, filler = true)).withUpcoming(anime, now)

        assertEquals(listOf(1, 2, 3, 4), items.map { it.number })
        assertEquals(listOf(true, false, false, false), items.map { it.aired })
        assertEquals(true, items[0].filler)
        assertEquals("Episode title 1", items[0].title)
        val friday = ZonedDateTime.of(2026, 10, 9, 23, 0, 0, 0, jst)
        assertEquals(friday, items[1].date)
        assertEquals(friday.plusWeeks(2), items[3].date)
    }

    @Test
    fun unknownEpisodeCountShowsOnlyTheNextOne() {
        val anime = Fixtures.airing.copy(broadcast = fridays, episodes = 0, status = "Currently Airing")
        assertEquals(listOf(1, 2, 3), listOf(aired(1), aired(2)).withUpcoming(anime, now).map { it.number })
    }

    @Test
    fun finishedOrUnknownScheduleShowsOnlyAired() {
        val finished = Fixtures.airing.copy(broadcast = fridays, episodes = 12, status = "Finished Airing")
        assertEquals(listOf(1, 2), listOf(aired(2), aired(1)).withUpcoming(finished, now).map { it.number })
        assertEquals(listOf(1), listOf(aired(1)).withUpcoming(null, now).map { it.number })
    }
}
