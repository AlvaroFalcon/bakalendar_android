package com.frostfel.animelist.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import com.frostfel.animelist.model.AnimeWithPreferences
import kotlinx.coroutines.flow.Flow

interface AnimeDbRepository {
    fun seasonPagingSource(): PagingSource<Int, AnimeWithPreferences>
    fun getAllFavFlow(): Flow<List<AnimeWithPreferences>>
    suspend fun getAllFav(): List<AnimeWithPreferences>
    suspend fun setStarred(malId: Int, starred: Boolean)
    fun findAnimeWithPreferencesByIdLiveData(id: Int): LiveData<AnimeWithPreferences?>
}
