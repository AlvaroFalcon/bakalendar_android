package com.frostfel.animelist

import com.frostfel.animelist.model.Anime

interface AnimeListNavigation {
    fun navigateToAnimeListFragment()
    fun navigateToAnimeDetail(anime: Anime)
}