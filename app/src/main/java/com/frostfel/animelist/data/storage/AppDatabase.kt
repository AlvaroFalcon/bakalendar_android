package com.frostfel.animelist.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.frostfel.animelist.data.dao.AnimeDao
import com.frostfel.animelist.data.dao.RemoteKeyDao
import com.frostfel.animelist.data.typeconverters.DatabaseTypeConverters
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimePreferences
import com.frostfel.animelist.model.RemoteKey

@Database(entities = [Anime::class, RemoteKey::class, AnimePreferences::class], version = 2)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}