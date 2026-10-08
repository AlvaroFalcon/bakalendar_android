package com.frostfel.animelist.views.season_list.adapter

import androidx.core.view.isVisible
import com.frostfel.animelist.databinding.AnimeListRowBinding
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.getNextBroadcastString
import com.frostfel.animelist.views.season_list.decorator.AnimeListItemDecorator
import com.frostfel.animelist.views.utils.loadCached

/** Binds the compact row (image on the left) used by the list mode and the weekly calendar. */
object AnimeRowBinder {
    fun bind(
        binding: AnimeListRowBinding,
        item: AnimeWithPreferences,
        airTime: String?,
        onClickAnime: (AnimeWithPreferences) -> Unit,
        onClickFav: (AnimeWithPreferences) -> Unit,
    ) {
        val anime = item.anime
        binding.airTime.isVisible = airTime != null
        binding.airTime.text = airTime
        binding.animeTitle.text = anime.title
        val nextEpisode = anime.getNextBroadcastString(binding.root.context)
        binding.nextEpisode.isVisible = nextEpisode.isNotBlank()
        binding.nextEpisode.text = nextEpisode
        binding.description.text = anime.synopsis
        binding.image.loadCached(anime.images.webp.largeImageUrl)
        if (binding.genreContainer.itemDecorationCount == 0) {
            binding.genreContainer.addItemDecoration(AnimeListItemDecorator())
        }
        binding.genreContainer.adapter = GenreListAdapter().apply { setData(anime.genres) }
        binding.favoriteButton.setState(item.userPreferences?.starred ?: false)
        binding.favoriteButton.setOnClickListener { onClickFav(item) }
        binding.root.setOnClickListener { onClickAnime(item) }
    }
}
