package com.frostfel.animelist.injection

import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.usecase.GetSeasonNowUseCaseImpl
import com.frostfel.animelist.usecase.ShouldReloadDataUseCaseImpl
import com.frostfel.animelist.usecase.interfaces.GetSeasonNowUseCase
import com.frostfel.animelist.usecase.interfaces.ShouldReloadDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    @Provides
    @Singleton
    fun provideGetSeasonNowUseCase(apiServices: ApiServices, shouldReloadDataUseCase: ShouldReloadDataUseCase): GetSeasonNowUseCase {
        return GetSeasonNowUseCaseImpl(apiServices, shouldReloadDataUseCase)
    }

    @Provides
    @Singleton
    fun provideShouldReloadDataUseCase(): ShouldReloadDataUseCase {
        return ShouldReloadDataUseCaseImpl()
    }
}