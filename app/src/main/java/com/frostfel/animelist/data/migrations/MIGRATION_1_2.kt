package com.frostfel.animelist.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS RemoteKey (
                id TEXT NOT NULL PRIMARY KEY,
                prevKey INTEGER,
                nextKey INTEGER,
                currentPage INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                animeId INTEGER NOT NULL
            )
        """.trimIndent())

        database.execSQL("ALTER TABLE Anime ADD COLUMN page INTEGER NOT NULL DEFAULT 0")

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS user_anime_preferences (
                malId INTEGER NOT NULL,
                starred INTEGER NOT NULL DEFAULT 0,
                starredAt INTEGER,
                PRIMARY KEY(malId)
            )
        """.trimIndent())

        database.execSQL("""
            INSERT INTO user_anime_preferences (malId, starred, starredAt)
            SELECT malId, 1, CAST((strftime('%s','now') * 1000) AS INTEGER)
            FROM anime
        """.trimIndent())
    }
}