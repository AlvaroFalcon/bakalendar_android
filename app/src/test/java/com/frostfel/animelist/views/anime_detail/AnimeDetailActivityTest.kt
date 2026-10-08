package com.frostfel.animelist.views.anime_detail

import android.content.Intent
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.frostfel.animelist.R
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.testing.AppTestSetup
import com.frostfel.animelist.testing.AppTestSetup.waitUntil
import com.frostfel.animelist.testing.Fixtures
import com.frostfel.animelist.views.utils.SwipeToDismissLayout
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private fun launch(anime: Anime) = ActivityScenario.launch<AnimeDetailActivity>(
        Intent(ApplicationProvider.getApplicationContext(), AnimeDetailActivity::class.java)
            .putExtra(AnimeDetailActivity.ANIME_EXTRA, AnimeWithPreferences(anime, null))
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
                waitUntil { activity.findViewById<android.widget.TextView>(R.id.anime_title).text.isNotEmpty() }
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
}
