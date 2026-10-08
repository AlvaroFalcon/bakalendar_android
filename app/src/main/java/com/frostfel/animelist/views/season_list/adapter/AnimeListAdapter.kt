package com.frostfel.animelist.views.season_list.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.databinding.AnimeListItemBinding
import com.frostfel.animelist.databinding.AnimeListRowBinding
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.getNextBroadcastString
import com.frostfel.animelist.views.season_list.decorator.AnimeListItemDecorator
import com.frostfel.animelist.views.utils.loadCached

/** Shows the season either as big cards or as a compact list ([listMode]). */
class AnimeListAdapter(
    private val onClickAnime: (anime: AnimeWithPreferences) -> Unit,
    private val onClickFav: (animeWithPreferences: AnimeWithPreferences) -> Unit,
) : ListAdapter<AnimeWithPreferences, RecyclerView.ViewHolder>(AnimeComparator) {

    var listMode: Boolean = false
        @SuppressLint("NotifyDataSetChanged") // every row changes layout
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override fun getItemViewType(position: Int): Int = if (listMode) TYPE_ROW else TYPE_CARD

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ROW) {
            RowViewHolder(AnimeListRowBinding.inflate(inflater, parent, false))
        } else {
            CardViewHolder(AnimeListItemBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is CardViewHolder -> holder.bind(item, onClickAnime, onClickFav)
            is RowViewHolder -> AnimeRowBinder.bind(
                holder.binding, item, airTime = null, onClickAnime, onClickFav
            )
        }
    }

    class RowViewHolder(val binding: AnimeListRowBinding) : RecyclerView.ViewHolder(binding.root)

    class CardViewHolder(private val binding: AnimeListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: AnimeWithPreferences,
            onClickAnime: (anime: AnimeWithPreferences) -> Unit,
            onClickFav: (item: AnimeWithPreferences) -> Unit,
        ) {
            with(item.anime) {
                binding.header.headerTitleText.text =
                    this.getNextBroadcastString(binding.root.context)
                binding.animeTitle.text = this.title
                binding.description.text = this.synopsis
                binding.image.loadCached(this.images.webp.largeImageUrl)
                val adapter = GenreListAdapter()
                if (binding.genreContainer.itemDecorationCount == 0) binding.genreContainer.addItemDecoration(
                    AnimeListItemDecorator()
                )
                adapter.setData(this.genres)
                binding.genreContainer.adapter = adapter
                binding.header.favoriteButton.setState(item.userPreferences?.starred ?: false)
                binding.header.favoriteButton.setOnClickListener { onClickFav(item) }
                binding.root.setOnClickListener { onClickAnime(item) }
            }
        }
    }

    object AnimeComparator : DiffUtil.ItemCallback<AnimeWithPreferences>() {
        override fun areItemsTheSame(oldItem: AnimeWithPreferences, newItem: AnimeWithPreferences): Boolean {
            return oldItem.anime.malId == newItem.anime.malId
        }

        override fun areContentsTheSame(oldItem: AnimeWithPreferences, newItem: AnimeWithPreferences): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        const val TYPE_CARD = 0
        const val TYPE_ROW = 1
    }
}
