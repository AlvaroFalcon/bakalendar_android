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

    private fun ChipGroup.chip(name: String) =
        (0 until childCount).map { getChildAt(it) as Chip }.single { it.text == name }

    @Test
    fun selectedGenresShowAnimeWithAnyOfThem() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val list = activity.findViewById<RecyclerView>(R.id.recylcerView)
                val chips = activity.findViewById<ChipGroup>(R.id.genreFilter)
                waitUntil { list.adapter!!.itemCount == 3 && chips.childCount > 0 }
                assertEquals("Fantasy", (chips.getChildAt(0) as Chip).text)

                // Drama: only Mushoku
                chips.chip("Drama").performClick()
                waitUntil { list.adapter!!.itemCount == 1 }

                // Drama or Comedy: Mushoku and Spy x Family
                chips.chip("Comedy").performClick()
                waitUntil { list.adapter!!.itemCount == 2 }
                assertEquals(true, chips.chip("Drama").isChecked)
                assertEquals(true, chips.chip("Comedy").isChecked)

                // Deselect both: everything again
                chips.chip("Drama").performClick()
                chips.chip("Comedy").performClick()
                waitUntil { list.adapter!!.itemCount == 3 }
                assertEquals(false, chips.chip("Comedy").isChecked)
            }
        }
    }
}
