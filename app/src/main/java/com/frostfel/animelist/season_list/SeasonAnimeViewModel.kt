package com.frostfel.animelist.season_list

import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.usecase.interfaces.GetSeasonNowUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeasonAnimeViewModel @Inject constructor(
    private val getSeasonNowUseCase: GetSeasonNowUseCase
) : ViewModel(), LifecycleObserver {
    val animeData = MutableLiveData<List<Anime>>()

    fun retrieveData(context: Context) {
        viewModelScope.launch {
            animeData.postValue(getSeasonNowUseCase.invoke(context))
        }
    }
}