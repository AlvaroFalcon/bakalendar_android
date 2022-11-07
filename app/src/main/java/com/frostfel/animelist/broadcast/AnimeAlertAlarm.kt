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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AnimeAlertAlarm : BroadcastReceiver() {
    @Inject
    lateinit var animeDbRepository: AnimeDbRepository
    private var notificationManager: NotificationManagerCompat? = null

    override fun onReceive(p0: Context?, p1: Intent?) {
        val favList: List<Anime> = animeDbRepository.getAllNoLive()
        val tapResultIntent = Intent(p0, MainActivity::class.java)
        tapResultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent =
            getActivity(p0, 0, tapResultIntent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        if (favList.isEmpty()) return
        val text = if (favList.size > 1) {
            "Some of your favorite animes are coming out today!"
        } else {
            "${favList.first().title} is coming out today!"
        }

        val notification = p0?.let {
            NotificationCompat.Builder(it, "fav_anime")
                .setContentTitle("Animes coming out today!")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()
        }
        notificationManager = p0?.let { NotificationManagerCompat.from(it) }
        notificationManager?.notify(favList.first().malId, notification!!)
    }
}