package com.frostfel.animelist.data.repository.impl

import com.frostfel.animelist.data.dao.RemoteKeyDao
import com.frostfel.animelist.data.repository.RemoteKeyRepository
import com.frostfel.animelist.model.RemoteKey
import javax.inject.Inject

class RemoteKeyRepositoryImpl @Inject constructor(private val remoteKeyDao: RemoteKeyDao): RemoteKeyRepository {
    override suspend fun getById(id: String) = remoteKeyDao.getById(id) ?: RemoteKey(id, 0, 0, 0, 0, 0)

    override suspend fun insert(remoteKey: RemoteKey) = remoteKeyDao.insert(remoteKey)

    override suspend fun deleteById(id: String) = remoteKeyDao.deleteById(id)

}