package com.frostfel.animelist

import android.view.View
import com.frostfel.animelist.model.Anime

interface AnimeListNavigation {
    fun navigateToAnimeDetail(anime: Anime)
}