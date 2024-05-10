package com.frostfel.animelist.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.frostfel.animelist.model.RemoteKey

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RemoteKey)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKey>)

    @Query("SELECT * FROM remoteKey WHERE id = :id")
    suspend fun getById(id: String): RemoteKey?
    @Query("Select createdAt from remoteKey order by createdAt DESC LIMIT 1")
    suspend fun getCreationTime(): Long?

    @Query("SELECT * FROM remoteKey where id = :animeId")
    suspend fun getByAnimeId(animeId: Int): RemoteKey?

    @Query("DELETE FROM remoteKey")
    suspend fun clearAll()

    @Query("DELETE FROM remoteKEy WHERE id = :id")
    suspend fun deleteById(id: String)
}