package com.frostfel.animelist.views.season_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.views.season_list.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonAnimeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    animeRepository: AnimeRepository,
    private val animeDbRepository: AnimeDbRepository
) : ViewModel() {
    val isFav: Boolean = savedStateHandle[SeasonAnimeFragment.IS_FAV_PARAM] ?: false

    private val query = MutableStateFlow("")

    val animeList: Flow<PagingData<AnimeWithPreferences>> = combine(
        animeRepository.getAnimeList(isFav).cachedIn(viewModelScope),
        query
    ) { data, filter ->
        if (filter.isBlank()) data
        else data.filter { it.anime.title?.contains(filter.trim(), ignoreCase = true) == true }
    }

    fun setQuery(text: String) {
        query.value = text
    }

    /** Returns true when the anime has just been added to favourites. */
    fun onFavTap(item: AnimeWithPreferences): Boolean {
        val starred = item.userPreferences?.starred?.not() ?: true
        viewModelScope.launch {
            animeDbRepository.setStarred(item.anime.malId, starred)
        }
        return starred
    }
}
