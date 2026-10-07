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
    fun matchesTitleAndGenre() {
        assertTrue(action.matches("", null))
        assertTrue(action.matches(" frie ", null))
        assertTrue(action.matches("", "Fantasy"))
        assertTrue(action.matches("frieren", "Adventure"))
        assertFalse(action.matches("frieren", "Comedy"))
        assertFalse(comedy.matches("", "Fantasy"))
    }
}
