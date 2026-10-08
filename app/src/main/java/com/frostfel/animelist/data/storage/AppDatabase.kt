package com.frostfel.animelist.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.frostfel.animelist.data.dao.AnimeDao
import com.frostfel.animelist.data.typeconverters.DatabaseTypeConverters
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimePreferences

@Database(entities = [Anime::class, AnimePreferences::class], version = 3)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}
