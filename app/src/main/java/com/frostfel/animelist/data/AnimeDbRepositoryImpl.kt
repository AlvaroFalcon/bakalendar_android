package com.frostfel.animelist.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frostfel.animelist.model.Anime
import javax.inject.Inject

class AnimeDbRepositoryImpl @Inject constructor(private val animeDao: AnimeDao) :
    AnimeDbRepository {
    override suspend fun addAnime(anime: Anime) {
        animeDao.insertAll(anime)
    }

    override suspend fun removeAnime(anime: Anime) {
        animeDao.deleteAnime(anime)
    }

    override suspend fun addOrRemove(anime: Anime) {
        val dbAnime: Anime? = animeDao.findAnimeById(anime.malId)
        if(dbAnime != null) {
            removeAnime(anime)
        } else {
            addAnime(anime)
        }
    }

    override fun getAll(): LiveData<List<Anime>> {
        return animeDao.getAll()
    }
}