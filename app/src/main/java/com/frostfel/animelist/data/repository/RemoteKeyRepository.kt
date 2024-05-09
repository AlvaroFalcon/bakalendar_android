package com.frostfel.animelist.data.repository

import com.frostfel.animelist.model.RemoteKey

interface RemoteKeyRepository {
    suspend fun insert(item: RemoteKey)
    suspend fun getById(id: String): RemoteKey
    suspend fun deleteById(id: String)
}