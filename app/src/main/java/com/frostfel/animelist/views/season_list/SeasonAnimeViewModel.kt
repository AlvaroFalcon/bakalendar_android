package com.frostfel.animelist.views.season_list

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.views.season_list.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonAnimeViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val animeDbRepository: AnimeDbRepository
) : ViewModel(), LifecycleObserver {
    var isFav = false
    val filterText : MutableLiveData<String> = MutableLiveData("")
    fun retrieveData(filter: String): Flow<PagingData<AnimeWithPreferences>> {
        return animeRepository.getAnimeList(isFav).asFlow()
            .map { it.filter { item -> item.anime.title?.contains(filter, ignoreCase = true) == true } }
            .cachedIn(viewModelScope)
    }

    fun onFavTap(anime: Anime) {
        viewModelScope.launch {
            //TODO: fix this pls
            animeDbRepository.setStarred(anime.malId, true)
        }
    }
}