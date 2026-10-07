package com.frostfel.animelist.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.frostfel.animelist.MainActivity
import com.frostfel.animelist.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

object FavouritesNotifier {
    const val CHANNEL_ID = "fav_anime"
    private const val NOTIFICATION_ID = 1

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun notify(context: Context, upcoming: List<UpcomingEpisode>) {
        if (upcoming.isEmpty() || !canNotify(context)) return

        val times = upcoming.map { formatAiringTime(it.airsAt) }
        val first = upcoming.first()
        val text = context.resources.getQuantityString(
            R.plurals.notification_text,
            upcoming.size,
            first.anime.title ?: "",
            times.first(),
            upcoming.size
        )
        val style = NotificationCompat.InboxStyle()
        upcoming.zip(times).forEach { (episode, time) ->
            style.addLine(context.getString(R.string.notification_line, episode.anime.title ?: "", time))
        }

        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(text)
            .setStyle(if (upcoming.size > 1) style else NotificationCompat.BigTextStyle().bigText(text))
            .setSmallIcon(R.drawable.ic_star)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission revoked between the check and the call.
        }
    }

    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** "16:00" for today, "Sat 01:05" otherwise, in the user's timezone. */
    internal fun formatAiringTime(
        airsAt: ZonedDateTime,
        zone: ZoneId = ZoneId.systemDefault(),
        locale: Locale = Locale.getDefault()
    ): String {
        val local = airsAt.withZoneSameInstant(zone)
        val time = local.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
        return if (local.toLocalDate() == LocalDate.now(zone)) time
        else "${local.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)} $time"
    }
}
