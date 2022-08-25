package com.frostfel.animelist.model

import android.content.Context
import com.frostfel.animelist.R
import com.frostfel.animelist.data.DayOfWeek
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class Broadcast(
    @SerializedName("day") val day: String,
    @SerializedName("time") val time: String,
    @SerializedName("timezone") val timeZone: String,
    @SerializedName("string") val stringValue: String,
)

fun Broadcast.getNextBroadcastString(context: Context): String {
    val day = DayOfWeek.from(this.day)
    val broadcastHour = this.time.split(":")[0].toInt()
    val broadcastMinute = this.time.split(":")[1].toInt()
    val todayZoned = LocalDateTime.now().atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneId.of(this.timeZone))
    val zonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneId.of(this.timeZone)).withHour(broadcastHour)
        .withMinute(broadcastMinute).withSecond(0)
    val nextBroadcastDate = when {
        day.value > zonedDateTime.dayOfWeek.value -> {
            // broadcast day after today
            val broadcastDate =
                zonedDateTime.plusDays((day.value - zonedDateTime.dayOfWeek.value).toLong())
            broadcastDate
        }
        day.value == zonedDateTime.dayOfWeek.value -> {
            // broadcast the same day
            val broadcastDate = if (broadcastHour < zonedDateTime.hour) {
                // this already happened
                zonedDateTime.plusDays(7L)
            } else {
                // airing today
                zonedDateTime
            }
            broadcastDate
        }
        else -> {
            // broadcast already happened, must be next week
            val daysForNextEpisode = 7 - (zonedDateTime.dayOfWeek.value - day.value)
            val broadcastDate = zonedDateTime.plusDays(daysForNextEpisode.toLong())
            broadcastDate
        }
    }
    val daysLeftForNextEpisode = ChronoUnit.DAYS.between(todayZoned, nextBroadcastDate)
    val hoursLeftForNewEpisode = ChronoUnit.HOURS.between(todayZoned, nextBroadcastDate) % 24
    val minutesLeftForNewEpisode = ChronoUnit.MINUTES.between(todayZoned, nextBroadcastDate) % 60
    val airingMinutesLeft = context.resources.getQuantityString(
        R.plurals.airing_minutes,
        minutesLeftForNewEpisode.toInt(),
        minutesLeftForNewEpisode.toInt()
    )
    val airingHoursLeft = context.resources.getQuantityString(
        R.plurals.airing_hours,
        hoursLeftForNewEpisode.toInt(),
        hoursLeftForNewEpisode.toInt()
    )
    val airingDaysLeft = context.resources.getQuantityString(
        R.plurals.airing_days,
        daysLeftForNextEpisode.toInt(),
        daysLeftForNextEpisode.toInt()
    )

    return if (daysLeftForNextEpisode == 0L) {
        if (hoursLeftForNewEpisode == 0L) {
            context.getString(R.string.one_value_airing, airingMinutesLeft)
        } else {
            context.getString(R.string.two_values_airing, airingHoursLeft, airingMinutesLeft)
        }
    } else {
        context.getString(R.string.two_values_airing, airingDaysLeft, airingMinutesLeft)
    }
}