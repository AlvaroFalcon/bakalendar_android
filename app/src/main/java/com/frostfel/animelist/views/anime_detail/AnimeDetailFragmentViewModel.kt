package com.frostfel.animelist.views.anime_detail

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.model.AnimeWithPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailFragmentViewModel @Inject constructor(
    private val animeDbRepository: AnimeDbRepository
) : ViewModel(), LifecycleObserver {

    private val _animeId = MutableLiveData<Int>()
    fun setAnimeId(id: Int) {
        if (_animeId.value != id) {
            _animeId.value = id
        }
    }
    val anime: LiveData<AnimeWithPreferences?> = _animeId.switchMap { id ->
        animeDbRepository.findAnimeWithPreferencesByIdLiveData(id)
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