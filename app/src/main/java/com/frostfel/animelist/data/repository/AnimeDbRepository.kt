package com.frostfel.animelist.data.repository

import androidx.lifecycle.LiveData
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeWithPreferences
import kotlinx.coroutines.flow.Flow

interface AnimeDbRepository {
    fun seasonWithPreferencesFlow(): Flow<List<AnimeWithPreferences>>
    fun seasonFlow(): Flow<List<Anime>>
    fun calendarFlow(): Flow<List<AnimeWithPreferences>>
    fun getAllFavFlow(): Flow<List<AnimeWithPreferences>>
    suspend fun getAllFav(): List<AnimeWithPreferences>
    suspend fun setStarred(malId: Int, starred: Boolean)
    fun findAnimeWithPreferencesByIdLiveData(id: Int): LiveData<AnimeWithPreferences?>
}
