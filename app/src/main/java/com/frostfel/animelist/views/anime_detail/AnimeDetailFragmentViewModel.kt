package com.frostfel.animelist.views.anime_detail

import androidx.lifecycle.*
import com.frostfel.animelist.data.AnimeDbRepository
import com.frostfel.animelist.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailFragmentViewModel @Inject constructor(
    private val animeDbRepository: AnimeDbRepository
) : ViewModel(), LifecycleObserver {

    lateinit var favAnime: LiveData<List<Anime>>
    var anime: MutableLiveData<Anime> = MutableLiveData()

    fun initFavAnime(malId: Int) {
        favAnime = animeDbRepository.getById(malId)
    }

    fun onFavTap(anime: Anime) {
        viewModelScope.launch {
            animeDbRepository.addOrRemove(anime)
        }
    }
}