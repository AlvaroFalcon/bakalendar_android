package com.frostfel.animelist

import android.widget.ImageButton
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import com.frostfel.animelist.data.season.SeasonCacheStore
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.testing.AppTestSetup
import com.frostfel.animelist.testing.AppTestSetup.waitUntil
import com.frostfel.animelist.testing.Fixtures
import com.frostfel.animelist.views.calendar.CalendarActivity
import com.frostfel.animelist.views.season_list.adapter.AnimeListAdapter
import com.google.android.material.navigation.NavigationView
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class MainScreenTest {
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
        runBlocking {
            db.animeDao().insertAll((1..3).map { Fixtures.anime(it).copy(page = it) })
        }
        cacheStore.lastRefreshMillis = System.currentTimeMillis()
    }

    @Test
    fun displayModeButtonSwitchesBetweenCardsAndList() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val list = activity.findViewById<RecyclerView>(R.id.recylcerView)
                waitUntil { list.adapter!!.itemCount == 3 && list.childCount > 0 }
                assertEquals(AnimeListAdapter.TYPE_CARD, list.adapter!!.getItemViewType(0))

                activity.findViewById<ImageButton>(R.id.displayModeButton).performClick()
                waitUntil { list.findViewHolderForAdapterPosition(0) is AnimeListAdapter.RowViewHolder }
                // The row shows the countdown to the next episode, bottom right.
                val row = list.findViewHolderForAdapterPosition(0) as AnimeListAdapter.RowViewHolder
                assertTrue(row.binding.nextEpisode.isVisible)
                assertTrue(row.binding.nextEpisode.text.startsWith("Next episode in"))

                activity.findViewById<ImageButton>(R.id.displayModeButton).performClick()
                waitUntil { list.findViewHolderForAdapterPosition(0) is AnimeListAdapter.CardViewHolder }
            }
        }
    }

    @Test
    fun menuOpensTheCalendar() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val drawer = activity.findViewById<DrawerLayout>(R.id.drawerLayout)
                val navigation = activity.findViewById<NavigationView>(R.id.navigationView)
                activity.findViewById<ImageButton>(R.id.menuButton).performClick()
                // Robolectric does not draw, so the opening animation never runs: open it directly.
                drawer.openDrawer(GravityCompat.END, false)
                waitUntil { drawer.isDrawerOpen(navigation) }

                navigation.menu.performIdentifierAction(R.id.menu_calendar, 0)

                val next = shadowOf(activity).nextStartedActivity
                assertEquals(CalendarActivity::class.java.name, next.component?.className)
            }
        }
    }
}
