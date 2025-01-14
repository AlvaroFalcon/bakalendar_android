package com.frostfel.animelist.data.repository.impl

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import com.frostfel.animelist.data.dao.AnimeDao
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimePreferences
import com.frostfel.animelist.model.AnimeWithPreferences
import javax.inject.Inject

class AnimeDbRepositoryImpl @Inject constructor(private val animeDao: AnimeDao) :
    AnimeDbRepository {
    override suspend fun addAnime(anime: Anime) {
        animeDao.insert(anime)
    }

    override suspend fun addAllAnime(animes: List<Anime>) {
        animeDao.insertAll(animes)
    }

    override suspend fun removeAnime(anime: Anime) {
        animeDao.deleteAnime(anime)
    }

    override suspend fun addOrRemove(anime: Anime) {
        val dbAnime: Anime? = animeDao.findAnimeById(anime.malId)?.anime
        if(dbAnime != null) {
            removeAnime(anime)
        } else {
            addAnime(anime)
        }
    }

    override fun getById(id: Int): LiveData<List<AnimeWithPreferences>> {
        return animeDao.findAnimeByIdLiveData(id)
    }

    override fun getAll(): LiveData<List<AnimeWithPreferences>> {
        return animeDao.getAll()
    }

    override fun pagingSource(): PagingSource<Int, AnimeWithPreferences> {
        return animeDao.pagingSource()
    }

    override fun getAllNoLive(): List<AnimeWithPreferences> {
        return animeDao.getAllNoLive()
    }

    override fun getAllFav(): LiveData<List<AnimeWithPreferences>> {
        return animeDao.getAllFav()
    }

    override fun getAllFavNoLive(): List<AnimeWithPreferences> {
        return animeDao.getAllFavNoLive()
    }

    override fun setStarred(malId: Int, starred: Boolean) {
        if(starred) {
            animeDao.setStarred(AnimePreferences(malId, true, System.currentTimeMillis()))
        } else {
            animeDao.removeStarred(malId)
        }
    }

    override fun findAnimeWithPreferencesByIdLiveData(id: Int): LiveData<AnimeWithPreferences?> {
        return animeDao.findAnimeWithPreferencesByIdLiveData(id);
    }
}