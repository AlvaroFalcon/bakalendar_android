package com.frostfel.animelist.views.calendar

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.viewpager2.widget.ViewPager2
import com.frostfel.animelist.R
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.model.AnimePreferences
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.Broadcast
import com.frostfel.animelist.model.weekSchedule
import com.frostfel.animelist.testing.AppTestSetup
import com.frostfel.animelist.testing.AppTestSetup.waitUntil
import com.frostfel.animelist.testing.Fixtures
import com.google.android.material.chip.Chip
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class CalendarActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppDatabase

    private val fridays = Broadcast("Fridays", "23:00", "Asia/Tokyo", null)
    private val mondays = Broadcast("Mondays", "22:00", "Asia/Tokyo", null)

    @Before
    fun setUp() {
        hiltRule.inject()
        AppTestSetup.init()
        runBlocking {
            db.animeDao().insertAll(
                listOf(
                    Fixtures.anime(1, "Favourite on Friday").copy(broadcast = fridays, page = 1),
                    Fixtures.anime(2, "Other on Friday").copy(broadcast = fridays, page = 2),
                    Fixtures.anime(3, "On Monday").copy(broadcast = mondays, page = 3),
                )
            )
            db.animeDao().setStarred(AnimePreferences(1, true, 0L))
        }
    }

    /** Index of the weekday the Friday 23:00 JST slot falls on in this machine's timezone. */
    private val fridayPage: Int
        get() {
            val week = listOf(AnimeWithPreferences(Fixtures.anime(9).copy(broadcast = fridays), null))
                .weekSchedule(ZonedDateTime.now())
            return CalendarPagerAdapter.DAYS.indexOfFirst { week.getValue(it).isNotEmpty() }
        }

    private fun CalendarActivity.dayCount(page: Int): Int {
        val pager = findViewById<ViewPager2>(R.id.dayPager)
        pager.setCurrentItem(page, false)
        val pages = pager.getChildAt(0) as RecyclerView
        val holder = pages.findViewHolderForAdapterPosition(page) as? CalendarPagerAdapter.PageHolder
        return holder?.adapter?.itemCount ?: -1
    }

    @Test
    fun showsWhatAirsEachDayAndFiltersFavourites() {
        ActivityScenario.launch(CalendarActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val page = fridayPage
                waitUntil { activity.dayCount(page) == 2 }

                activity.findViewById<Chip>(R.id.favoritesOnly).performClick()
                waitUntil { activity.dayCount(page) == 1 }

                activity.findViewById<Chip>(R.id.favoritesOnly).performClick()
                waitUntil { activity.dayCount(page) == 2 }
            }
        }
    }
}
