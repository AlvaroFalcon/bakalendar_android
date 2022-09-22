package com.frostfel.animelist.data.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.frostfel.animelist.data.AnimeDao
import com.frostfel.animelist.data.typeconverters.DatabaseTypeConverters
import com.frostfel.animelist.model.Anime

@Database(entities = [Anime::class], version = 1)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
}