package com.frostfel.animelist.views.season_list

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.views.season_list.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class SeasonAnimeViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel(), LifecycleObserver {
    val filterText : MutableLiveData<String> = MutableLiveData("")
    fun retrieveData(filter: String): Flow<PagingData<Anime>> {
        return animeRepository.getAnimeList().asFlow()
            .map { it.filter { item -> item.title.contains(filter, ignoreCase = true) } }
            .cachedIn(viewModelScope)
    }
}