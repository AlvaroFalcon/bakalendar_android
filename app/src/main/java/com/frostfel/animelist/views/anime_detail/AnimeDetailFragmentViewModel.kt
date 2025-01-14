package com.frostfel.animelist.views.anime_detail

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.AnimeWithPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailFragmentViewModel @Inject constructor(
    private val animeDbRepository: AnimeDbRepository
) : ViewModel(), LifecycleObserver {

    lateinit var favAnime: LiveData<List<AnimeWithPreferences>>
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