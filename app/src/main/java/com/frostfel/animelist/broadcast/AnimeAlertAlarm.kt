package com.frostfel.animelist.broadcast

import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.frostfel.animelist.MainActivity
import com.frostfel.animelist.R
import com.frostfel.animelist.data.AnimeDbRepository
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.isAiringToday
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AnimeAlertAlarm : BroadcastReceiver() {
    @Inject
    lateinit var animeDbRepository: AnimeDbRepository
    private var notificationManager: NotificationManagerCompat? = null

    override fun onReceive(context: Context?, p1: Intent?) {
        val favList: List<Anime> = animeDbRepository.getAllNoLive()
        var todayAiring = addTodayAiringAnimesFrom(favList)
        val tapResultIntent = Intent(context, MainActivity::class.java)
        tapResultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent =
            getActivity(context, 0, tapResultIntent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        if (todayAiring.isEmpty()) return

        val notificationPluralString = context?.resources?.getQuantityString(
            R.plurals.notification_text,
            todayAiring.size,
            todayAiring.first().title ?: ""
        )

        val notificationTitle = context?.resources?.getString(R.string.notification_title)

        val notification = context?.let {
            NotificationCompat.Builder(it, "fav_anime")
                .setContentTitle(notificationTitle ?: "")
                .setContentText(notificationPluralString)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()
        }
        notificationManager = context?.let { NotificationManagerCompat.from(context) }
        notificationManager?.notify(favList.first().malId, notification!!)
    }

    private fun addTodayAiringAnimesFrom(favList: List<Anime>): List<Anime> {
        val list = arrayListOf<Anime>()
        favList.forEach {
            if (it.broadcast.isAiringToday()) {
                list.add(it)
            }
        }
        return list
    }
}