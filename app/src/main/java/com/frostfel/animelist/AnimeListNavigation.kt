package com.frostfel.animelist

import com.frostfel.animelist.model.AnimeWithPreferences

interface AnimeListNavigation {
    fun navigateToAnimeDetail(anime: AnimeWithPreferences)
}