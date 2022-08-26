package com.frostfel.animelist

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
) : ViewModel(), LifecycleObserver {
    lateinit var navigator: AnimeListNavigation
    fun initViewModel(animeListNavigation: AnimeListNavigation) {
        navigator = animeListNavigation
    }
}