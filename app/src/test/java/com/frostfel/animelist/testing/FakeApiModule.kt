package com.frostfel.animelist.testing

import com.frostfel.animelist.data.ApiServices
import com.frostfel.animelist.injection.ApiModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/** UI tests never hit the network: every Hilt test gets the fake API. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ApiModule::class])
object FakeApiModule {
    @Provides
    @Singleton
    fun fakeApi(): FakeApiServices = FakeApiServices()

    @Provides
    fun api(fake: FakeApiServices): ApiServices = fake
}
