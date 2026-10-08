package com.frostfel.animelist.views.anime_detail

import android.content.Intent
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.frostfel.animelist.R
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.RelatedEntry
import com.frostfel.animelist.model.RelationGroup
import com.frostfel.animelist.model.RelationsResponse
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.testing.AppTestSetup
import com.frostfel.animelist.testing.AppTestSetup.waitUntil
import com.frostfel.animelist.testing.FakeApiServices
import com.frostfel.animelist.testing.Fixtures
import com.frostfel.animelist.views.utils.SwipeToDismissLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class AnimeDetailActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppDatabase

    private val withTrailer = Fixtures.seasonResponse.data[0] // youtube_id YvNvvUeCztE
    private val withoutTrailer = Fixtures.seasonResponse.data[2]

    @Before
    fun setUp() {
        hiltRule.inject()
        AppTestSetup.init()
        runBlocking { db.animeDao().insertAll(listOf(withTrailer.copy(page = 1), withoutTrailer.copy(page = 2))) }
    }

    @Inject
    lateinit var api: FakeApiServices

    private fun launch(anime: Anime) = launch(anime.malId)

    private fun launch(malId: Int) = ActivityScenario.launch<AnimeDetailActivity>(
        AnimeDetailActivity.intent(ApplicationProvider.getApplicationContext(), malId)
    )

    @Test
    fun opensDetail() {
        launch(withTrailer).use { scenario ->
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }

    @Test
    fun trailerOpensYouTube() {
        launch(withTrailer).use { scenario ->
            scenario.onActivity { activity ->
                val trailer = activity.findViewById<View>(R.id.trailer)
                waitUntil { trailer.visibility == View.VISIBLE }
                trailer.performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(Intent.ACTION_VIEW, started.action)
                assertEquals("vnd.youtube:YvNvvUeCztE", started.dataString)
            }
        }
    }

    @Test
    fun noTrailerNoPlayer() {
        launch(withoutTrailer).use { scenario ->
            scenario.onActivity { activity ->
                waitUntil { activity.findViewById<CollapsingToolbarLayout>(R.id.collapsingToolbar).title != null }
                assertEquals(View.GONE, activity.findViewById<View>(R.id.trailer).visibility)
            }
        }
    }

    private fun SwipeToDismissLayout.drag(distance: Float, durationMs: Long) {
        val start = SystemClock.uptimeMillis()
        val x = width / 2f
        val y = height / 3f
        val steps = 10
        dispatchTouchEvent(MotionEvent.obtain(start, start, MotionEvent.ACTION_DOWN, x, y, 0))
        for (i in 1..steps) {
            val time = start + durationMs * i / steps
            dispatchTouchEvent(MotionEvent.obtain(start, time, MotionEvent.ACTION_MOVE, x, y + distance * i / steps, 0))
        }
        val end = start + durationMs
        dispatchTouchEvent(MotionEvent.obtain(start, end, MotionEvent.ACTION_UP, x, y + distance, 0))
    }

    @Test
    fun draggingDownClosesTheDetail() {
        launch(withTrailer).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<SwipeToDismissLayout>(R.id.dismissLayout)
                waitUntil { layout.height > 0 }
                layout.drag(distance = layout.height * 0.5f, durationMs = 400)
                waitUntil { activity.isFinishing }
            }
        }
    }

    @Test
    fun aShortDragSnapsBack() {
        launch(withTrailer).use { scenario ->
            scenario.onActivity { activity ->
                val layout = activity.findViewById<SwipeToDismissLayout>(R.id.dismissLayout)
                waitUntil { layout.height > 0 }
                layout.drag(distance = layout.height * 0.1f, durationMs = 1000)
                waitUntil { layout.getChildAt(0).translationY == 0f }
                assertFalse(activity.isFinishing)
            }
        }
    }

    private fun ChipGroup.texts() = (0 until childCount).map { (getChildAt(it) as Chip).text.toString() }

    @Test
    fun showsStatsNextEpisodeAndInformation() {
        launch(withTrailer).use { scenario ->
            scenario.onActivity { activity ->
                val stats = activity.findViewById<ChipGroup>(R.id.stats)
                waitUntil { stats.childCount > 0 }
                assertEquals(
                    "Kusuriya no Hitorigoto 3rd Season",
                    activity.findViewById<CollapsingToolbarLayout>(R.id.collapsingToolbar).title
                )
                assertEquals(listOf("8.65", "#90", "TV", "12 episodes", "Fall 2026"), stats.texts())
                assertEquals(listOf("Drama", "Mystery"), activity.findViewById<ChipGroup>(R.id.genres).texts())

                // Fridays 23:00 JST, currently airing: always has a next episode.
                assertTrue(activity.findViewById<View>(R.id.nextEpisodeCard).isVisible)
                assertTrue(activity.findViewById<TextView>(R.id.nextEpisodeWhen).text.endsWith("your time"))

                val info = activity.findViewById<LinearLayout>(R.id.infoRows)
                val text = (0 until info.childCount).mapNotNull { info.getChildAt(it) as? LinearLayout }
                    .associate { row -> (row.getChildAt(0) as TextView).text.toString() to (row.getChildAt(1) as TextView).text.toString() }
                assertEquals("The Apothecary Diaries Season 3", text["English title"])
                assertEquals("OLM", text["Studios"])
                assertEquals("Light novel", text["Source"])
                assertEquals("Historical, Medical", text["Themes"])
            }
        }
    }

    @Test
    fun favouriteButtonStarsTheAnime() {
        launch(withTrailer).use { scenario ->
            scenario.onActivity { activity ->
                val fab = activity.findViewById<ExtendedFloatingActionButton>(R.id.favoriteFab)
                waitUntil { activity.findViewById<ChipGroup>(R.id.stats).childCount > 0 }
                assertEquals("Add to favorites", fab.text.toString())

                fab.performClick()
                waitUntil { fab.text.toString() == "In favorites" }
                assertTrue(runBlocking { db.animeDao().getAllFav() }.any { it.anime.malId == withTrailer.malId })
            }
        }
    }

    @Test
    fun myAnimeListButtonAndReadMore() {
        launch(withTrailer).use { scenario ->
            scenario.onActivity { activity ->
                waitUntil { activity.findViewById<ChipGroup>(R.id.stats).childCount > 0 }
                activity.findViewById<View>(R.id.malButton).performClick()
                assertEquals(withTrailer.url, shadowOf(activity).nextStartedActivity.dataString)

                val description = activity.findViewById<TextView>(R.id.description)
                assertEquals(5, description.maxLines)
                activity.findViewById<View>(R.id.readMore).performClick()
                assertEquals(Int.MAX_VALUE, description.maxLines)
            }
        }
    }

    @Test
    fun relatedAnimeOpenInTheAppAndMangaOnMyAnimeList() {
        api.relations = mapOf(
            withTrailer.malId to RelationsResponse(
                listOf(
                    RelationGroup("Adaptation", listOf(RelatedEntry(7, "manga", "The manga", "https://myanimelist.net/manga/7", "Manga", null))),
                    RelationGroup("Prequel", listOf(RelatedEntry(58514, "anime", "Season 2", "https://myanimelist.net/anime/58514", "TV", null))),
                )
            )
        )
        launch(withTrailer).use { scenario ->
            scenario.onActivity { activity ->
                val related = activity.findViewById<RecyclerView>(R.id.related)
                waitUntil { related.isVisible && related.adapter!!.itemCount == 2 && related.childCount == 2 }
                assertTrue(activity.findViewById<View>(R.id.relatedTitle).isVisible)

                val first = related.findViewHolderForAdapterPosition(0) as RelatedAdapter.ViewHolder
                assertEquals("Prequel", first.binding.relation.text)
                assertEquals("Season 2", first.binding.name.text)
                first.binding.root.performClick()
                val detail = shadowOf(activity).nextStartedActivity
                assertEquals(AnimeDetailActivity::class.java.name, detail.component?.className)
                assertEquals(58514, detail.getIntExtra(AnimeDetailActivity.ANIME_ID_EXTRA, -1))

                (related.findViewHolderForAdapterPosition(1) as RelatedAdapter.ViewHolder).binding.root.performClick()
                assertEquals("https://myanimelist.net/manga/7", shadowOf(activity).nextStartedActivity.dataString)
            }
        }
    }

    @Test
    fun noRelationsNoSection() {
        launch(withTrailer).use { scenario ->
            scenario.onActivity { activity ->
                waitUntil { activity.findViewById<ChipGroup>(R.id.stats).childCount > 0 }
                assertFalse(activity.findViewById<View>(R.id.related).isVisible)
                assertFalse(activity.findViewById<View>(R.id.relatedTitle).isVisible)
            }
        }
    }

    @Test
    fun animeThatIsNotCachedIsDownloaded() {
        api.byId = mapOf(58514 to Fixtures.anime(58514, "Kusuriya no Hitorigoto 2nd Season"))
        launch(58514).use { scenario ->
            scenario.onActivity { activity ->
                val title = activity.findViewById<CollapsingToolbarLayout>(R.id.collapsingToolbar)
                waitUntil { title.title == "Kusuriya no Hitorigoto 2nd Season" }
                assertFalse(activity.findViewById<View>(R.id.loadState).isVisible)
            }
        }
    }

    @Test
    fun downloadErrorCanBeRetried() {
        launch(424242).use { scenario ->
            scenario.onActivity { activity ->
                val retry = activity.findViewById<View>(R.id.loadRetry)
                waitUntil { retry.isVisible }
                assertTrue(activity.findViewById<View>(R.id.loadError).isVisible)

                api.byId = mapOf(424242 to Fixtures.anime(424242, "Back online"))
                retry.performClick()
                val title = activity.findViewById<CollapsingToolbarLayout>(R.id.collapsingToolbar)
                waitUntil { title.title == "Back online" }
                assertFalse(activity.findViewById<View>(R.id.loadState).isVisible)
            }
        }
    }
}
