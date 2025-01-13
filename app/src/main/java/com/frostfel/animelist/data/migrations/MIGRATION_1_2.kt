package com.frostfel.animelist.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Date

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the new RemoteKey table
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

        // Add the new column to the existing anime table
        database.execSQL("ALTER TABLE anime ADD COLUMN page INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE anime ADD COLUMN starredAt INTEGER DEFAULT NULL")

        // Update the existing data in the anime table
        database.execSQL("UPDATE anime SET starredAt = ${Date().time} WHERE starred = 0")
        database.execSQL("UPDATE anime SET starred = 1 WHERE starred = 0")
    }
}