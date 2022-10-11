package com.frostfel.animelist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.frostfel.animelist.model.Anime

@Dao
interface AnimeDao {
    @Insert
    suspend fun insertAll(vararg animes: Anime)

    @Delete
    suspend fun deleteAnime(anime: Anime)

    @Query("SELECT * FROM anime WHERE malId=:id ")
    suspend fun findAnimeById(id: Int): Anime?
}