package com.frostfel.animelist.testing

import android.content.Context
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.squareup.picasso.Picasso
import org.robolectric.Shadows.shadowOf
import java.time.Duration

object AppTestSetup {
    /** In the app these are initialised by content providers, which Robolectric does not run. */
    fun init() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        try {
            Picasso.setSingletonInstance(Picasso.Builder(context).build())
        } catch (alreadySet: IllegalStateException) {
        }
    }

    fun waitUntil(condition: () -> Boolean) {
        repeat(200) {
            // Advance the clock too, so animations (e.g. the drawer) can finish.
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(20))
            if (condition()) return
            Thread.sleep(20)
        }
        throw AssertionError("condition not met")
    }
}
