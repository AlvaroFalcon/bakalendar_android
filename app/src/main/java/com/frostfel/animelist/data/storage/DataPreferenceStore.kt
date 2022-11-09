package com.frostfel.animelist.data.storage

import android.content.Context
import com.frostfel.animelist.model.Anime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class DataPreferenceStore {
    companion object {
        private const val DATA_PREFERENCE_STORE_NAME = "DATA_PREFERENCE_STORE_NAME"
        private const val ANIME_LIST_DATA_KEY = "ANIME_LIST_DATA_KEY"
        private const val LAST_UPDATED_KEY = "LAST_UPDATED_KEY"
        private const val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS"
        private const val IS_FIRST_TIME = "IS_FIRST_TIME"
        private val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
        private val pattern = DateTimeFormatter.ofPattern(DATE_FORMAT)

        private fun Context.getStore() =
            getSharedPreferences(DATA_PREFERENCE_STORE_NAME, Context.MODE_PRIVATE)

        fun saveData(data: List<Anime>, context: Context) {
            // serialize data and save date
            val listString = Gson().toJson(data)
            with(context.getStore().edit()) {
                putString(ANIME_LIST_DATA_KEY, listString)
                putString(LAST_UPDATED_KEY, LocalDateTime.now().format(formatter))
                apply()
            }
        }

        fun isFirstTime(context: Context): Boolean {
            return context.getStore().getBoolean(IS_FIRST_TIME, true)
        }

        fun setFirstTime(context: Context) {
            with(context.getStore().edit()) {
                putBoolean(IS_FIRST_TIME, false)
            }
        }

        fun getLastUpdate(context: Context): LocalDateTime? {
            // retrieve last updated date
            val lastUpdated = context.getStore().getString(LAST_UPDATED_KEY, null) ?: return null
            return LocalDateTime.parse(lastUpdated, pattern)
        }

        fun getData(context: Context): List<Anime> {
            // retrieve saved data
            val dataString = context.getStore().getString(ANIME_LIST_DATA_KEY, "")
            val type = object : TypeToken<List<Anime>>() {}.type
            return Gson().fromJson(dataString, type) ?: listOf()
        }
    }
}