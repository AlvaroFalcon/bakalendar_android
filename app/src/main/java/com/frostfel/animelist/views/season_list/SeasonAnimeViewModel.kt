package com.frostfel.animelist.views.season_list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.data.season.SeasonRefresher
import com.frostfel.animelist.data.storage.DisplayModeStore
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.SortOrder
import com.frostfel.animelist.model.matches
import com.frostfel.animelist.model.sortedBy
import com.frostfel.animelist.views.season_list.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject

/** Whether the season is being downloaded, and the last download error if it failed. */
data class RefreshState(val refreshing: Boolean = false, val error: Throwable? = null)

@HiltViewModel
class SeasonAnimeViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    animeRepository: AnimeRepository,
    private val animeDbRepository: AnimeDbRepository,
    private val displayModeStore: DisplayModeStore,
    private val seasonRefresher: SeasonRefresher,
) : ViewModel() {
    val isFav: Boolean = savedStateHandle[SeasonAnimeFragment.IS_FAV_PARAM] ?: false

    private val query = MutableStateFlow("")

    val selectedGenres: StateFlow<Set<String>> = savedStateHandle
        .getStateFlow(SELECTED_GENRES_KEY, arrayListOf<String>())
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val sortOrder: StateFlow<SortOrder> = savedStateHandle
        .getStateFlow(SORT_ORDER_KEY, SortOrder.POPULARITY.name)
        .map { SortOrder.valueOf(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SortOrder.POPULARITY)

    val genres: Flow<List<String>> = animeRepository.getGenres(isFav)

    /** True for the compact list, false for big cards. */
    val listMode: StateFlow<Boolean> = displayModeStore.listMode

    private val _refreshState = MutableStateFlow(RefreshState())
    val refreshState: StateFlow<RefreshState> = _refreshState
    private var refreshJob: Job? = null

    val animeList: Flow<List<AnimeWithPreferences>> = combine(
        animeRepository.getAnimeList(isFav),
        query,
        selectedGenres,
        sortOrder,
    ) { items, text, genres, order ->
        items.filter { it.anime.matches(text, genres) }.sortedBy(order, ZonedDateTime.now())
    }

    val hasActiveFilter: Boolean
        get() = query.value.isNotBlank() || selectedGenres.value.isNotEmpty()

    /** Genres or a non-default order picked in the filters sheet. */
    val hasActiveSheetFilters: Flow<Boolean> = combine(selectedGenres, sortOrder) { genres, order ->
        genres.isNotEmpty() || order != SortOrder.POPULARITY
    }

    init {
        // Favourites live only in the local database; the season is refreshed when stale.
        if (!isFav) {
            viewModelScope.launch { if (seasonRefresher.needsRefresh()) refresh() }
        }
    }

    fun refresh() {
        if (isFav || refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _refreshState.value = RefreshState(refreshing = true)
            _refreshState.value = try {
                seasonRefresher.refreshSeason()
                RefreshState()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RefreshState(error = e)
            }
        }
    }

    fun toggleListMode() = displayModeStore.toggle()

    fun setQuery(text: String) {
        query.value = text
    }

    /** Toggles [genre]; the list shows anime with any of the selected genres. */
    fun onGenreTap(genre: String) {
        val current = selectedGenres.value
        val updated = if (genre in current) current - genre else current + genre
        savedStateHandle[SELECTED_GENRES_KEY] = ArrayList(updated)
    }

    fun setSortOrder(order: SortOrder) {
        savedStateHandle[SORT_ORDER_KEY] = order.name
    }

    fun clearSheetFilters() {
        savedStateHandle[SELECTED_GENRES_KEY] = arrayListOf<String>()
        savedStateHandle[SORT_ORDER_KEY] = SortOrder.POPULARITY.name
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
        const val SORT_ORDER_KEY = "sort_order"
    }
}
