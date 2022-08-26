package com.frostfel.animelist.views.season_list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.databinding.AnimeListItemBinding
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.getNextBroadcastString
import com.frostfel.animelist.views.season_list.decorator.AnimeListItemDecorator
import com.squareup.picasso.Picasso

class AnimeListAdapter(private val onClickAnime: (anime: Anime) -> Unit) : PagingDataAdapter<Anime, AnimeListAdapter.ViewHolder>(AnimeComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            AnimeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position)!!, onClickAnime)

    class ViewHolder(private val binding: AnimeListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(anime: Anime, onClickAnime: (anime: Anime) -> Unit) {
            binding.headerTitleText.text = anime.broadcast.getNextBroadcastString(binding.root.context)
            binding.animeTitle.text = anime.title
            binding.description.text = anime.synopsis
            Picasso.get().load(anime.images.webp.largeImageUrl).into(binding.image)
            val adapter = GenreListAdapter()
            if (binding.genreContainer.itemDecorationCount == 0) binding.genreContainer.addItemDecoration(AnimeListItemDecorator())
            adapter.setData(anime.genres)
            binding.genreContainer.adapter = adapter
            binding.root.setOnClickListener { onClickAnime(anime) }
        }
    }

    object AnimeComparator: DiffUtil.ItemCallback<Anime>() {
        override fun areItemsTheSame(oldItem: Anime, newItem: Anime): Boolean {
            return oldItem.malId == newItem.malId
        }

        override fun areContentsTheSame(oldItem: Anime, newItem: Anime): Boolean {
            return oldItem == newItem
        }
    }
}