package com.frostfel.animelist.views.anime_detail

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.testing.Fixtures
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class AnimeDetailActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun opensDetail() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), AnimeDetailActivity::class.java)
            .putExtra(AnimeDetailActivity.ANIME_EXTRA, AnimeWithPreferences(Fixtures.airing, null))

        ActivityScenario.launch<AnimeDetailActivity>(intent).use { scenario ->
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
    }
}
