package com.frostfel.animelist.injection

import android.content.Context
import androidx.room.Room
import com.frostfel.animelist.data.dao.AnimeDao
import com.frostfel.animelist.data.migrations.MIGRATION_1_2
import com.frostfel.animelist.data.migrations.MIGRATION_2_3
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.data.repository.impl.AnimeDbRepositoryImpl
import com.frostfel.animelist.data.storage.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    fun provideAnimeDao(appDatabase: AppDatabase): AnimeDao {
        return appDatabase.animeDao()
    }

    @Provides
    @Singleton
    fun provideAnimeDbRepository(animeDao: AnimeDao): AnimeDbRepository {
        return AnimeDbRepositoryImpl(animeDao)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "BakalendarDB"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()
    }
}