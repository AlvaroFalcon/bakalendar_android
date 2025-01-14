package com.frostfel.animelist.injection

import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.data.storage.AppDatabase
import com.frostfel.animelist.views.season_list.repository.AnimeRepository
import com.frostfel.animelist.views.season_list.repository.AnimeRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
    fun provideAnimeRepository(apiServices: ApiServices, animeDbRepository: AnimeDbRepository,appDatabase: AppDatabase): AnimeRepository {
        return AnimeRepositoryImpl(apiServices, animeDbRepository, appDatabase)
    }
}