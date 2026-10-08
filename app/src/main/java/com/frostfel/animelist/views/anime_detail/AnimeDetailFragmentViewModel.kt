package com.frostfel.animelist.views.anime_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.frostfel.animelist.data.repository.AnimeDbRepository
import com.frostfel.animelist.data.repository.AnimeDetailRepository
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.RelatedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AnimeDetailFragmentViewModel @Inject constructor(
    private val animeDbRepository: AnimeDbRepository,
    private val animeDetailRepository: AnimeDetailRepository,
) : ViewModel() {

    private val _animeId = MutableLiveData<Int>()
    private val _related = MutableLiveData<List<RelatedItem>>(emptyList())
    private val _loadFailed = MutableLiveData(false)

    /** Related anime and manga; empty while loading, offline or when there are none. */
    val related: LiveData<List<RelatedItem>> = _related

    /** True when the anime is not cached and could not be downloaded either. */
    val loadFailed: LiveData<Boolean> = _loadFailed

    val anime: LiveData<AnimeWithPreferences?> = _animeId.switchMap { id ->
        animeDbRepository.findAnimeWithPreferencesByIdLiveData(id)
    }

    fun setAnimeId(id: Int) {
        if (_animeId.value == id) return
        _animeId.value = id
        load(id)
    }

    fun retry() {
        _animeId.value?.let(::load)
    }

    private fun load(id: Int) {
        _loadFailed.value = false
        viewModelScope.launch {
            try {
                animeDetailRepository.ensureCached(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Could not load anime $id")
                _loadFailed.value = true
                return@launch
            }
            try {
                _related.value = animeDetailRepository.getRelations(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Could not load relations of $id")
            }
        }
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
