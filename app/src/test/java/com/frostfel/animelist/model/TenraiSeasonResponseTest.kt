package com.frostfel.animelist.model

import com.frostfel.animelist.data.typeconverters.DatabaseTypeConverters
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Parses a real `GET /v1/seasons/now` response captured from Tenrai. */
class TenraiSeasonResponseTest {

    private val response: SeasonResponse = javaClass.classLoader!!
        .getResourceAsStream("tenrai_seasons_now.json")
        .reader()
        .use { Gson().fromJson(it, SeasonResponse::class.java) }

    @Test
    fun parsesPagination() {
        assertTrue(response.pagination.hasNextPage)
        assertEquals(5, response.pagination.lastVisiblePage)
        assertEquals(25, response.pagination.currentPage.perPage)
    }

    @Test
    fun parsesAnimeWithKnownBroadcast() {
        val anime = response.data[0]
        assertEquals(61987, anime.malId)
        assertEquals("Kusuriya no Hitorigoto 3rd Season", anime.title)
        assertEquals(12, anime.episodes)
        assertEquals("Fridays", anime.broadcast.day)
        assertEquals("23:00", anime.broadcast.time)
        assertEquals("Asia/Tokyo", anime.broadcast.timeZone)
        assertTrue(anime.images.webp.largeImageUrl.endsWith(".webp"))
        assertTrue(anime.genres.isNotEmpty())
    }

    @Test
    fun handlesUnknownBroadcastAndAiredDate() {
        val noBroadcast = response.data[1]
        assertNull(noBroadcast.broadcast.day)
        assertNull(noBroadcast.broadcast.timeZone)
        assertNull(noBroadcast.nextEpisodeAt())

        val notYetAired = response.data[2]
        assertNull(notYetAired.aired.from)
        // null numeric fields fall back to 0
        assertEquals(0, notYetAired.episodes)
        assertEquals(0.0, notYetAired.score, 0.0)
    }

    @Test
    fun survivesRoomTypeConverters() {
        val converters = DatabaseTypeConverters()
        response.data.forEach { anime ->
            assertEquals(anime.aired, converters.toAired(converters.fromAired(anime.aired)))
            assertEquals(anime.broadcast, converters.toBroadcast(converters.fromBroadcast(anime.broadcast)))
            assertEquals(anime.genres, converters.toGenericMalData(converters.fromGenericMalDataList(anime.genres)))
        }
    }

}
