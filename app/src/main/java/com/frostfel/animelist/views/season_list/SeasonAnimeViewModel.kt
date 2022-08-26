package com.frostfel.animelist.views.season_list

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.views.season_list.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SeasonAnimeViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel(), LifecycleObserver {

    fun retrieveData(): LiveData<PagingData<Anime>> {
        return animeRepository.getAnimeList().cachedIn(viewModelScope)
    }
}