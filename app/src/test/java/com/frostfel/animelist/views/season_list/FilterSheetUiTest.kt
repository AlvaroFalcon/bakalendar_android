package com.frostfel.animelist.views.season_list

import android.app.Activity
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import com.frostfel.animelist.MainActivity
import com.frostfel.animelist.R
import com.frostfel.animelist.data.season.SeasonCacheStore
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.Broadcast
import com.frostfel.animelist.testing.AppTestSetup
import com.frostfel.animelist.testing.AppTestSetup.waitUntil
import com.frostfel.animelist.testing.Fixtures
import com.frostfel.animelist.views.season_list.adapter.AnimeListAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class FilterSheetUiTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var cacheStore: SeasonCacheStore

    @Before
    fun setUp() {
        hiltRule.inject()
        AppTestSetup.init()
        // Fresh cache: no network refresh during the test.
        cacheStore.lastRefreshMillis = System.currentTimeMillis()
    }

    private fun insert(animes: List<Anime>) = runBlocking {
        db.animeDao().insertAll(animes.mapIndexed { index, anime -> anime.copy(page = index + 1) })
    }

    private fun Activity.list() = findViewById<RecyclerView>(R.id.recylcerView)
    private fun Activity.ids() = (list().adapter as AnimeListAdapter).currentList.map { it.anime.malId }

    private fun Activity.openFilters(): BottomSheetDialog {
        findViewById<ImageButton>(R.id.filterButton).performClick()
        waitUntil { (ShadowDialog.getLatestDialog() as? BottomSheetDialog)?.isShowing == true }
        return ShadowDialog.getLatestDialog() as BottomSheetDialog
    }

    private fun BottomSheetDialog.genre(name: String): Chip {
        val group = findViewById<ChipGroup>(R.id.genreFilter)!!
        waitUntil { group.childCount > 0 }
        return (0 until group.childCount).map { group.getChildAt(it) as Chip }.single { it.text == name }
    }

    @Test
    fun selectedGenresShowAnimeWithAnyOfThem() {
        insert(
            listOf(
                Fixtures.anime(1, "Frieren", genres = listOf("Adventure", "Fantasy")),
                Fixtures.anime(2, "Spy x Family", genres = listOf("Comedy", "Action")),
                Fixtures.anime(3, "Mushoku", genres = listOf("Fantasy", "Drama")),
            )
        )
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitUntil { activity.ids().size == 3 }
                val sheet = activity.openFilters()
                val genres = sheet.findViewById<ChipGroup>(R.id.genreFilter)!!
                waitUntil { genres.childCount > 0 }
                assertEquals("Fantasy", (genres.getChildAt(0) as Chip).text)

                sheet.genre("Drama").performClick()
                waitUntil { activity.ids() == listOf(3) }

                sheet.genre("Comedy").performClick()
                waitUntil { activity.ids() == listOf(2, 3) }
                assertTrue(sheet.genre("Drama").isChecked && sheet.genre("Comedy").isChecked)

                sheet.findViewById<View>(R.id.clearButton)!!.performClick()
                waitUntil { activity.ids() == listOf(1, 2, 3) && !sheet.genre("Comedy").isChecked }
            }
        }
    }

    @Test
    fun sortsByNextEpisode() {
        val japan = ZoneId.of("Asia/Tokyo")
        fun weekday(daysFromNow: Long): String {
            val day = ZonedDateTime.now(japan).plusDays(daysFromNow).dayOfWeek.name.lowercase()
            return day.replaceFirstChar { it.uppercase() } + "s"
        }
        insert(
            listOf(
                Fixtures.anime(1, "Unknown").copy(broadcast = Broadcast(null, null, null, null)),
                Fixtures.anime(2, "In two days").copy(broadcast = Broadcast(weekday(2), "12:00", "Asia/Tokyo", null)),
                Fixtures.anime(3, "Tomorrow").copy(broadcast = Broadcast(weekday(1), "12:00", "Asia/Tokyo", null)),
            )
        )
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                waitUntil { activity.ids() == listOf(1, 2, 3) }
                val sheet = activity.openFilters()

                sheet.findViewById<Chip>(R.id.sortNextEpisode)!!.performClick()
                waitUntil { activity.ids() == listOf(3, 2, 1) }

                sheet.findViewById<Chip>(R.id.sortPopularity)!!.performClick()
                waitUntil { activity.ids() == listOf(1, 2, 3) }
            }
        }
    }

    @Test
    fun changingTheFilterGoesBackToTheTop() {
        insert(
            listOf(
                Fixtures.anime(1, genres = listOf("Drama")),
                Fixtures.anime(2, genres = listOf("Drama")),
            ) + (10..19).map { Fixtures.anime(it, genres = listOf("Action")) }
        )
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val list = activity.list()
                val layoutManager = list.layoutManager as LinearLayoutManager
                waitUntil { activity.ids().size == 12 }
                val sheet = activity.openFilters()

                sheet.genre("Action").performClick()
                waitUntil { activity.ids().size == 10 }
                list.scrollToPosition(9)
                waitUntil { layoutManager.findFirstVisibleItemPosition() > 0 }

                sheet.genre("Action").performClick()
                waitUntil { activity.ids().size == 12 && layoutManager.findFirstVisibleItemPosition() == 0 }
            }
        }
    }
}
