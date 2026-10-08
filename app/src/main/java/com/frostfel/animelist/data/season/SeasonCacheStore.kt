package com.frostfel.animelist.data.season

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeasonCacheStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("season_cache", Context.MODE_PRIVATE)

    var lastRefreshMillis: Long
        get() = prefs.getLong(LAST_REFRESH_KEY, 0L)
        set(value) = prefs.edit().putLong(LAST_REFRESH_KEY, value).apply()

    private companion object {
        const val LAST_REFRESH_KEY = "last_refresh_millis"
    }
}
