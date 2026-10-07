package com.frostfel.animelist

import android.content.Context
import androidx.lifecycle.ViewModel
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.notifications.FavouritesNotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val animeDbRepository: AnimeDbRepository
) : ViewModel() {
    lateinit var navigator: AnimeListNavigation
    fun initViewModel(animeListNavigation: AnimeListNavigation) {
        navigator = animeListNavigation
    }

    fun initNotifications(context: Context) {
        FavouritesNotificationScheduler.schedule(context.applicationContext)
    }

    suspend fun hasFavourites(): Boolean = animeDbRepository.getAllFav().isNotEmpty()
}
