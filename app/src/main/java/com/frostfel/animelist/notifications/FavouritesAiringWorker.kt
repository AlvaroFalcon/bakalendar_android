package com.frostfel.animelist.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.data.season.SeasonRefresher
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import java.time.ZonedDateTime

/** Runs once a day and tells the user which favourites air in the next 24 hours. */
class FavouritesAiringWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun animeDbRepository(): AnimeDbRepository
        fun seasonRefresher(): SeasonRefresher
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
        refreshIfStale(deps.seasonRefresher())
        val upcoming = deps.animeDbRepository().getAllFav().airingWithin(ZonedDateTime.now())
        FavouritesNotifier.notify(applicationContext, upcoming)
        return Result.success()
    }

    /** Best effort: schedules rarely change, so cached data is fine when offline. */
    private suspend fun refreshIfStale(seasonRefresher: SeasonRefresher) {
        try {
            if (seasonRefresher.needsRefresh()) seasonRefresher.refreshSeason()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Could not refresh season before notifying")
        }
    }
}
