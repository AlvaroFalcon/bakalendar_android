package com.frostfel.animelist.data.repository.impl

import androidx.lifecycle.LiveData
import com.frostfel.animelist.data.dao.AnimeDao
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimePreferences
import com.frostfel.animelist.model.AnimeWithPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnimeDbRepositoryImpl @Inject constructor(private val animeDao: AnimeDao) :
    AnimeDbRepository {

    override fun seasonWithPreferencesFlow(): Flow<List<AnimeWithPreferences>> {
        return animeDao.seasonWithPreferencesFlow()
    }

    override fun seasonFlow(): Flow<List<Anime>> {
        return animeDao.seasonFlow()
    }

    override fun calendarFlow(): Flow<List<AnimeWithPreferences>> {
        return animeDao.calendarFlow()
    }

    override fun getAllFavFlow(): Flow<List<AnimeWithPreferences>> {
        return animeDao.getAllFavFlow()
    }

    override suspend fun getAllFav(): List<AnimeWithPreferences> {
        return animeDao.getAllFav()
    }

    override suspend fun setStarred(malId: Int, starred: Boolean) {
        if (starred) {
            animeDao.setStarred(AnimePreferences(malId, true, System.currentTimeMillis()))
        } else {
            animeDao.removeStarred(malId)
        }
    }

    override fun findAnimeWithPreferencesByIdLiveData(id: Int): LiveData<AnimeWithPreferences?> {
        return animeDao.findAnimeWithPreferencesByIdLiveData(id)
    }
}
