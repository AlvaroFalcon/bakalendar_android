package com.frostfel.animelist.data.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimePreferences

@Dao
interface AnimeDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertAll(animes: List<Anime> )

    @Insert
    suspend fun insert(anime: Anime)

    @Query("DELETE FROM anime")
    suspend fun clearAll()

    @Delete
    suspend fun deleteAnime(anime: Anime)

    @Update(entity = Anime::class)
    fun update(anime: Anime)

    @Query("SELECT * FROM anime WHERE malId=:id ")
    suspend fun findAnimeById(id: Int): Anime?

    @Query("SELECT * FROM anime WHERE malId=:id")
    fun findAnimeByIdLiveData(id: Int): LiveData<List<Anime>>

    @Query("SELECT * FROM anime order by page")
    fun pagingSource(): PagingSource<Int, Anime>

    @Query("SELECT * FROM anime")
    fun getAll(): LiveData<List<Anime>>

    @Query("SELECT * FROM anime")
    fun getAllNoLive(): List<Anime>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    fun setStarred(animePreferences: AnimePreferences)

    @Query("DELETE FROM user_anime_preferences WHERE malId = :malId")
    fun removeStarred(malId: Int)

        @Query("""
        SELECT a.*
        FROM anime a
        INNER JOIN user_anime_preferences u 
            ON a.malId = u.malId
        WHERE u.starred = 1
    """)

    fun getAllFavNoLive(): List<Anime>

    @Query("""
        SELECT a.*
        FROM anime a
        INNER JOIN user_anime_preferences u 
            ON a.malId = u.malId
        WHERE u.starred = 1
    """)

    fun getAllFav(): LiveData<List<Anime>>
}