package com.frostfel.animelist.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules the daily "airing in the next 24 hours" check with WorkManager, which survives
 * reboots and app updates (the old AlarmManager alarm did not).
 */
object FavouritesNotificationScheduler {
    private const val WORK_NAME = "favourites_airing_daily"
    private val NOTIFY_AT: LocalTime = LocalTime.of(10, 0)

    fun schedule(context: Context) {
        FavouritesNotifier.createChannel(context)
        cancelLegacyAlarm(context)
        val request = PeriodicWorkRequestBuilder<FavouritesAiringWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntilNext(NOTIFY_AT, ZonedDateTime.now()).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    internal fun delayUntilNext(time: LocalTime, now: ZonedDateTime): Duration {
        var next = now.with(time).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next)
    }

    /** Versions before 1.8 used a repeating alarm on AnimeAlertAlarm; remove it. */
    private fun cancelLegacyAlarm(context: Context) {
        val intent = Intent().setComponent(
            ComponentName(context.packageName, "com.frostfel.animelist.broadcast.AnimeAlertAlarm")
        )
        val pending = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        context.getSystemService(AlarmManager::class.java).cancel(pending)
        pending.cancel()
    }
}
