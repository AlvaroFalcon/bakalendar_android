package com.frostfel.animelist

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class MainActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setUp() {
        // In the app WorkManager is initialised by androidx.startup, which Robolectric does not run.
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun startsAndSchedulesTheDailyNotification() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertEquals(Lifecycle.State.RESUMED, scenario.state)
        }
        val work = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork("favourites_airing_daily").get()
        assertEquals(listOf(WorkInfo.State.ENQUEUED), work.map { it.state })
    }
}
