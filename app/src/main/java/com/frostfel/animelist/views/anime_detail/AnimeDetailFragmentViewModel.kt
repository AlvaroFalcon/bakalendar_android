package com.frostfel.animelist.views.anime_detail

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.frostfel.animelist.data.AnimeDbRepository
import com.frostfel.animelist.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
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
}