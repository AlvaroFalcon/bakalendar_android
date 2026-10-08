package com.frostfel.animelist.views.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.CalendarEntry
import com.frostfel.animelist.model.weekSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val animeDbRepository: AnimeDbRepository,
) : ViewModel() {

    val favouritesOnly: StateFlow<Boolean> = savedStateHandle.getStateFlow(FAVOURITES_ONLY_KEY, false)

    val week: Flow<Map<DayOfWeek, List<CalendarEntry>>> = combine(
        animeDbRepository.calendarFlow(),
        favouritesOnly
    ) { items, onlyFavourites ->
        items.filter { !onlyFavourites || it.userPreferences?.starred == true }
            .weekSchedule(ZonedDateTime.now())
    }

    fun setFavouritesOnly(enabled: Boolean) {
        savedStateHandle[FAVOURITES_ONLY_KEY] = enabled
    }

    /** Returns true when the anime has just been added to favourites. */
    fun onFavTap(item: AnimeWithPreferences): Boolean {
        val starred = item.userPreferences?.starred?.not() ?: true
        viewModelScope.launch {
            animeDbRepository.setStarred(item.anime.malId, starred)
        }
        return starred
    }

    private companion object {
        const val FAVOURITES_ONLY_KEY = "favourites_only"
    }
}
