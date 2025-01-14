package com.frostfel.animelist

import com.frostfel.animelist.model.Anime

interface AnimeListNavigation {
    fun navigateToAnimeDetail(anime: Anime)
}