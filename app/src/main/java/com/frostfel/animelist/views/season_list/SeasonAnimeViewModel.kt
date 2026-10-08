package com.frostfel.animelist.views.season_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.data.storage.DisplayModeStore
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.matches
import com.frostfel.animelist.views.season_list.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonAnimeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    animeRepository: AnimeRepository,
    private val animeDbRepository: AnimeDbRepository,
    private val displayModeStore: DisplayModeStore,
) : ViewModel() {
    val isFav: Boolean = savedStateHandle[SeasonAnimeFragment.IS_FAV_PARAM] ?: false

    private val query = MutableStateFlow("")

    val selectedGenres: StateFlow<Set<String>> = savedStateHandle
        .getStateFlow(SELECTED_GENRES_KEY, arrayListOf<String>())
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val genres: Flow<List<String>> = animeRepository.getGenres(isFav)

    /** True for the compact list, false for big cards. */
    val listMode: StateFlow<Boolean> = displayModeStore.listMode

    fun toggleListMode() = displayModeStore.toggle()

    val animeList: Flow<PagingData<AnimeWithPreferences>> = combine(
        animeRepository.getAnimeList(isFav).cachedIn(viewModelScope),
        query,
        selectedGenres
    ) { data, text, genres ->
        if (text.isBlank() && genres.isEmpty()) data
        else data.filter { it.anime.matches(text, genres) }
    }

    val hasActiveFilter: Boolean
        get() = query.value.isNotBlank() || selectedGenres.value.isNotEmpty()

    fun setQuery(text: String) {
        query.value = text
    }

    /** Toggles [genre]; the list shows anime with any of the selected genres. */
    fun onGenreTap(genre: String) {
        val current = selectedGenres.value
        val updated = if (genre in current) current - genre else current + genre
        savedStateHandle[SELECTED_GENRES_KEY] = ArrayList(updated)
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
        const val SELECTED_GENRES_KEY = "selected_genres"
    }
}
