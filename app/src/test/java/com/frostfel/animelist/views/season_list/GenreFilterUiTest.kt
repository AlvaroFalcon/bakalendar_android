package com.frostfel.animelist.views.season_list

import android.content.Context
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.frostfel.animelist.MainActivity
import com.frostfel.animelist.R
import com.frostfel.animelist.data.season.SeasonCacheStore
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.testing.Fixtures
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.squareup.picasso.Picasso
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class GenreFilterUiTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var cacheStore: SeasonCacheStore

    @Before
    fun setUp() {
        hiltRule.inject()
        val context = ApplicationProvider.getApplicationContext<Context>()
        // In the app these are initialised by content providers, which Robolectric does not run.
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        try {
            Picasso.setSingletonInstance(Picasso.Builder(context).build())
        } catch (alreadySet: IllegalStateException) {
        }
        runBlocking {
            db.animeDao().insertAll(
                listOf(
                    Fixtures.anime(1, "Frieren", genres = listOf("Adventure", "Fantasy")).copy(page = 1),
                    Fixtures.anime(2, "Spy x Family", genres = listOf("Comedy", "Action")).copy(page = 2),
                    Fixtures.anime(3, "Mushoku", genres = listOf("Fantasy", "Drama")).copy(page = 3),
                )
            )
        }
        // Fresh cache: no network refresh during the test.
        cacheStore.lastRefreshMillis = System.currentTimeMillis()
    }

    private fun waitUntil(condition: () -> Boolean) {
        repeat(200) {
            shadowOf(Looper.getMainLooper()).idle()
            if (condition()) return
            Thread.sleep(20)
        }
        throw AssertionError("condition not met")
    }

    @Test
    fun selectingAGenreFiltersTheListAndTappingAgainClearsIt() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val list = activity.findViewById<RecyclerView>(R.id.recylcerView)
                val chips = activity.findViewById<ChipGroup>(R.id.genreFilter)
                waitUntil { list.adapter!!.itemCount == 3 && chips.childCount > 0 }

                val names = (0 until chips.childCount).map { (chips.getChildAt(it) as Chip).text.toString() }
                assertEquals("Fantasy", names.first())

                val fantasy = chips.getChildAt(0) as Chip
                fantasy.performClick()
                waitUntil { list.adapter!!.itemCount == 2 }
                assertEquals(true, fantasy.isChecked)

                fantasy.performClick()
                waitUntil { list.adapter!!.itemCount == 3 }
                assertEquals(false, fantasy.isChecked)
            }
        }
    }
}
