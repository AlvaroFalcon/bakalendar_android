package com.frostfel.animelist.injection

import android.content.Context
import androidx.room.Room
import com.frostfel.animelist.data.AnimeDao
import com.frostfel.animelist.data.AnimeDbRepository
import com.frostfel.animelist.data.AnimeDbRepositoryImpl
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
        ).allowMainThreadQueries().build()
    }
}