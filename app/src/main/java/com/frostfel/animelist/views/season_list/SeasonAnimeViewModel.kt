package com.frostfel.animelist.views.season_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.matches
import com.frostfel.animelist.views.season_list.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonAnimeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    animeRepository: AnimeRepository,
    private val animeDbRepository: AnimeDbRepository
) : ViewModel() {
    val isFav: Boolean = savedStateHandle[SeasonAnimeFragment.IS_FAV_PARAM] ?: false

    private val query = MutableStateFlow("")

    val selectedGenre: StateFlow<String?> = savedStateHandle.getStateFlow(SELECTED_GENRE_KEY, null)

    val genres: Flow<List<String>> = animeRepository.getGenres(isFav)

    val animeList: Flow<PagingData<AnimeWithPreferences>> = combine(
        animeRepository.getAnimeList(isFav).cachedIn(viewModelScope),
        query,
        selectedGenre
    ) { data, text, genre ->
        if (text.isBlank() && genre == null) data
        else data.filter { it.anime.matches(text, genre) }
    }

    val hasActiveFilter: Boolean
        get() = query.value.isNotBlank() || selectedGenre.value != null

    fun setQuery(text: String) {
        query.value = text
    }

    /** Selecting the active genre again clears the filter. */
    fun onGenreTap(genre: String) {
        savedStateHandle[SELECTED_GENRE_KEY] = genre.takeIf { it != selectedGenre.value }
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
        const val SELECTED_GENRE_KEY = "selected_genre"
    }
}
