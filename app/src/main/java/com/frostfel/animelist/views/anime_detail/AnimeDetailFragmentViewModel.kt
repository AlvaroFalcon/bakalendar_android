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
import com.frostfel.animelist.model.EpisodeItem
import com.frostfel.animelist.model.StreamingLink
import com.frostfel.animelist.model.withUpcoming
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.ZonedDateTime
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
    private val _recommendations = MutableLiveData<List<RelatedItem>>(emptyList())
    private val _episodes = MutableLiveData<EpisodesState>(EpisodesState.Idle)
    private var episodesJob: Job? = null
    private val _loadFailed = MutableLiveData(false)

    /** Related anime and manga; empty while loading, offline or when there are none. */
    val related: LiveData<List<RelatedItem>> = _related

    /** "You might also like"; empty for most new shows. */
    val recommendations: LiveData<List<RelatedItem>> = _recommendations

    val episodes: LiveData<EpisodesState> = _episodes

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
            _related.value = optional("relations of $id") { animeDetailRepository.getRelations(id) }
            _recommendations.value =
                optional("recommendations of $id") { animeDetailRepository.getRecommendations(id) }
        }
    }

    /** Loaded the first time the Episodes tab is opened (and on retry). */
    fun loadEpisodes() {
        val id = _animeId.value ?: return
        val current = _episodes.value
        if (current is EpisodesState.Loaded || episodesJob?.isActive == true) return
        _episodes.value = EpisodesState.Loading
        episodesJob = viewModelScope.launch {
            _episodes.value = try {
                coroutineScope {
                    val aired = async { animeDetailRepository.getEpisodes(id) }
                    val streaming = async {
                        optional("streaming of $id") { animeDetailRepository.getStreaming(id) }
                    }
                    val items = aired.await().withUpcoming(anime.value?.anime, ZonedDateTime.now())
                    EpisodesState.Loaded(items, streaming.await())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w(e, "Could not load episodes of $id")
                EpisodesState.Error
            }
        }
    }

    private suspend fun <T> optional(what: String, load: suspend () -> List<T>): List<T> =
        try {
            load()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Could not load $what")
            emptyList()
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

sealed interface EpisodesState {
    object Idle : EpisodesState
    object Loading : EpisodesState
    object Error : EpisodesState
    data class Loaded(val episodes: List<EpisodeItem>, val streaming: List<StreamingLink>) : EpisodesState
}
