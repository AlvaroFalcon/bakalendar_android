package com.frostfel.animelist.views.anime_detail

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
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
    val anime: LiveData<AnimeWithPreferences?> = Transformations.switchMap(_animeId) { id ->
        animeDbRepository.findAnimeWithPreferencesByIdLiveData(id)
    }

    fun onFavTap(item: AnimeWithPreferences) {
        viewModelScope.launch {
            animeDbRepository.setStarred(item.anime.malId, item.userPreferences?.starred?.not() ?: true)
        }
    }
}