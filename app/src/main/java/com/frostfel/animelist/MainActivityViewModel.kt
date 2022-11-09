package com.frostfel.animelist

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.frostfel.animelist.broadcast.AnimeAlertAlarm
import com.frostfel.animelist.data.storage.DataPreferenceStore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
) : ViewModel(), LifecycleObserver {
    lateinit var navigator: AnimeListNavigation
    fun initViewModel(animeListNavigation: AnimeListNavigation) {
        navigator = animeListNavigation
    }

    fun initNotifications(context: Context?) {
        context?.let {
            if (DataPreferenceStore.isFirstTime(it)) {
                createNotificationChannel(it)
                DataPreferenceStore.setFirstTime(context)
                var alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                var alarmIntent: PendingIntent =
                    Intent(context, AnimeAlertAlarm::class.java).let { intent ->
                        PendingIntent.getBroadcast(context, 0, intent, 0)
                    }

                val calendar: Calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, 10)
                }

                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    alarmIntent
                )

            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("fav_anime", "Anime alerts channel", importance).apply {
            description = "Notification for anime alerts schedule"
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}