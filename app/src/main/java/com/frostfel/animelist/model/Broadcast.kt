package com.frostfel.animelist.model

import android.os.Parcelable
import com.frostfel.animelist.data.DayOfWeek
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

@Parcelize
data class Broadcast(
    @SerializedName("day") val day: String?,
    @SerializedName("time") val time: String?,
    @SerializedName("timezone") val timeZone: String?,
    @SerializedName("string") val stringValue: String?,
) : Parcelable

/**
 * First weekly slot at or after [now] (and not before [notBefore], in the broadcast's own
 * timezone), or null when the schedule is unknown.
 */
fun Broadcast.nextAiringAfter(now: ZonedDateTime, notBefore: LocalDate? = null): ZonedDateTime? {
    val dayOfWeek = DayOfWeek.from(day).toJavaDayOfWeek() ?: return null
    val localTime = time?.let { runCatching { LocalTime.parse(it) }.getOrNull() } ?: return null
    val zone = broadcastZone()
    val nowInZone = now.withZoneSameInstant(zone)
    val from = maxOf(nowInZone.toLocalDate(), notBefore ?: LocalDate.MIN)
    val candidate = from.with(TemporalAdjusters.nextOrSame(dayOfWeek)).atTime(localTime).atZone(zone)
    return if (candidate.isBefore(nowInZone)) candidate.plusWeeks(1) else candidate
}

// MAL broadcasts are always JST; the API returns a null timezone when the schedule is unknown.
private fun Broadcast.broadcastZone(): ZoneId =
    timeZone?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: ZoneId.of("Asia/Tokyo")
