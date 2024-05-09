package com.frostfel.animelist.data.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.frostfel.animelist.model.Anime

@Dao
interface AnimeDao {
    @Insert
    suspend fun insertAll(vararg animes: Anime)

    @Delete
    suspend fun deleteAnime(anime: Anime)

    @Update(entity = Anime::class)
    fun update(anime: Anime)

    @Query("SELECT * FROM anime WHERE malId=:id ")
    suspend fun findAnimeById(id: Int): Anime?

    @Query("SELECT * FROM anime WHERE malId=:id")
    fun findAnimeByIdLiveData(id: Int): LiveData<List<Anime>>

    @Query("SELECT * FROM anime")
    fun getAll(): LiveData<List<Anime>>

    @Query("SELECT * FROM anime")
    fun getAllNoLive(): List<Anime>
}