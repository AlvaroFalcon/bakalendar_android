package com.frostfel.animelist.model

import com.frostfel.animelist.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenreFilterTest {
    private val action = Fixtures.anime(1, "Frieren", genres = listOf("Adventure", "Fantasy"))
    private val comedy = Fixtures.anime(2, "Spy x Family", genres = listOf("Comedy", "Action"))
    private val fantasy = Fixtures.anime(3, "Mushoku", genres = listOf("Fantasy", "Drama"))

    @Test
    fun genresSortedByCountThenName() {
        assertEquals(
            listOf("Fantasy", "Action", "Adventure", "Comedy", "Drama"),
            listOf(action, comedy, fantasy).genresByPopularity()
        )
    }

    @Test
    fun matchesTitleAndAnyOfTheSelectedGenres() {
        assertTrue(action.matches("", emptySet()))
        assertTrue(action.matches(" frie ", emptySet()))
        assertTrue(action.matches("", setOf("Fantasy")))
        assertTrue(action.matches("", setOf("Comedy", "Adventure")))
        assertTrue(action.matches("frieren", setOf("Adventure")))
        assertFalse(action.matches("frieren", setOf("Comedy")))
        assertFalse(comedy.matches("", setOf("Fantasy", "Drama")))
    }

    @Test
    fun sortsByNextEpisodeKeepingPopularityForTies() {
        val now = java.time.ZonedDateTime.of(2026, 10, 7, 12, 0, 0, 0, java.time.ZoneId.of("Asia/Tokyo")) // Wednesday
        fun item(id: Int, day: String?) = AnimeWithPreferences(
            Fixtures.anime(id).copy(broadcast = Broadcast(day, day?.let { "20:00" }, "Asia/Tokyo", null)),
            null
        )
        val items = listOf(item(1, null), item(2, "Fridays"), item(3, "Wednesdays"), item(4, "Fridays"))

        assertEquals(listOf(1, 2, 3, 4), items.sortedBy(SortOrder.POPULARITY, now).map { it.anime.malId })
        assertEquals(listOf(3, 2, 4, 1), items.sortedBy(SortOrder.NEXT_EPISODE, now).map { it.anime.malId })
    }
}
