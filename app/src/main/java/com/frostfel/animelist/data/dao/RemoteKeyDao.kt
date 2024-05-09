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

    @Query("SELECT * FROM remoteKey WHERE id = :id")
    suspend fun getById(id: String): RemoteKey

    @Query("DELETE FROM remoteKEy WHERE id = :id")
    suspend fun deleteById(id: String)
}