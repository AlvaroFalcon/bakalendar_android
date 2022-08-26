package com.frostfel.animelist.views.anime_detail

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.frostfel.animelist.model.Anime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AnimeDetailFragmentViewModel @Inject constructor(
) : ViewModel(), LifecycleObserver {
    var anime: MutableLiveData<Anime> = MutableLiveData()


}