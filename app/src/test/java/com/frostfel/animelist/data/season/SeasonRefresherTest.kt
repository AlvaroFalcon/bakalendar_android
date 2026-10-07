package com.frostfel.animelist.data.season

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frostfel.animelist.data.repository.impl.AnimeDbRepositoryImpl
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.testing.FakeApiServices
import com.frostfel.animelist.testing.Fixtures
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SeasonRefresherTest {
    private lateinit var db: AppDatabase
    private lateinit var api: FakeApiServices
    private lateinit var refresher: SeasonRefresher
    private lateinit var favourites: AnimeDbRepositoryImpl
    private var now = 1_000_000_000L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        api = FakeApiServices()
        refresher = SeasonRefresher(api, db, SeasonCacheStore(context)).also { it.clock = { now } }
        favourites = AnimeDbRepositoryImpl(db.animeDao())
    }

    @After
    fun tearDown() = db.close()

    private suspend fun seasonIds(): List<Int> {
        val source = db.animeDao().seasonPagingSource()
        val result = source.load(
            androidx.paging.PagingSource.LoadParams.Refresh(null, 500, false)
        ) as androidx.paging.PagingSource.LoadResult.Page
        return result.data.map { it.anime.malId }
    }

    @Test
    fun downloadsEveryPageInApiOrder() = runTest {
        api.season = (1..120).map { Fixtures.anime(1000 - it) }

        refresher.refreshSeason()

        assertEquals(listOf(1, 2, 3), api.requestedPages)
        assertEquals(api.season.map { it.malId }, seasonIds())
    }

    @Test
    fun continuingSeriesGoAfterTheNewOnes() = runTest {
        api.season = listOf(
            Fixtures.anime(1, "One Piece", season = "fall", year = "1999"),
            Fixtures.anime(2, season = "fall", year = "2026"),
            Fixtures.anime(3, "Conan", season = "winter", year = "1996"),
            Fixtures.anime(4, season = "fall", year = "2026"),
            Fixtures.anime(5, "No season", season = null, year = null),
        )

        refresher.refreshSeason()

        assertEquals(listOf(2, 4, 1, 3, 5), seasonIds())
    }

    @Test
    fun needsRefreshWhenEmptyOrOlderThan12Hours() = runTest {
        assertTrue(refresher.needsRefresh())
        api.season = listOf(Fixtures.anime(1))
        refresher.refreshSeason()
        assertFalse(refresher.needsRefresh())
        now += SeasonRefresher.MAX_AGE_MILLIS + 1
        assertTrue(refresher.needsRefresh())
    }

    @Test
    fun favouritesSurviveSeasonChange() = runTest {
        api.season = listOf(Fixtures.anime(1, "Old fav"), Fixtures.anime(2, "Old"))
        refresher.refreshSeason()
        favourites.setStarred(1, true)

        api.season = listOf(Fixtures.anime(3, "New"))
        refresher.refreshSeason()

        assertEquals(listOf(3), seasonIds())
        assertEquals(listOf("Old fav"), favourites.getAllFav().map { it.anime.title })
    }

    @Test
    fun airingFavouritesOutsideTheSeasonAreUpdated() = runTest {
        api.season = listOf(Fixtures.anime(1, "Two-cour show"))
        refresher.refreshSeason()
        favourites.setStarred(1, true)

        api.season = listOf(Fixtures.anime(2))
        api.byId = mapOf(1 to Fixtures.anime(1, "Two-cour show, updated"))
        refresher.refreshSeason()

        assertEquals(listOf("Two-cour show, updated"), favourites.getAllFav().map { it.anime.title })
        assertEquals(listOf(2), seasonIds())
    }

    @Test
    fun failedRefreshKeepsTheCache() = runTest {
        api.season = (1..60).map { Fixtures.anime(it) }
        refresher.refreshSeason()

        api.season = (100..160).map { Fixtures.anime(it) }
        api.failOnPage = 2
        try {
            refresher.refreshSeason()
            fail("expected IOException")
        } catch (e: IOException) {
            // expected
        }

        assertEquals((1..60).toList(), seasonIds())
    }

    @Test
    fun favouritesAreNewestFirstAndCanBeRemoved() = runTest {
        api.season = (1..3).map { Fixtures.anime(it) }
        refresher.refreshSeason()

        favourites.setStarred(1, true)
        Thread.sleep(5)
        favourites.setStarred(3, true)
        assertEquals(listOf(3, 1), favourites.getAllFavFlow().first().map { it.anime.malId })

        favourites.setStarred(3, false)
        assertEquals(listOf(1), favourites.getAllFav().map { it.anime.malId })
    }
}
