package com.frostfel.animelist

import android.view.View
import com.frostfel.animelist.model.Anime

interface AnimeListNavigation {
    fun navigateToAnimeListFragment()
    fun navigateToAnimeDetail(anime: Anime)
}