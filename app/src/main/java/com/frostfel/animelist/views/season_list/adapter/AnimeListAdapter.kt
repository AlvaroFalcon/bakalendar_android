    package com.frostfel.animelist.views.season_list.adapter

    import android.view.LayoutInflater
    import android.view.ViewGroup
    import androidx.paging.PagingDataAdapter
    import androidx.recyclerview.widget.DiffUtil
    import androidx.recyclerview.widget.RecyclerView
    import com.frostfel.animelist.databinding.AnimeListItemBinding
    import com.frostfel.animelist.model.AnimeWithPreferences
    import com.frostfel.animelist.model.getNextBroadcastString
    import com.frostfel.animelist.views.season_list.decorator.AnimeListItemDecorator
    import com.squareup.picasso.Picasso

    class AnimeListAdapter(
        private val onClickAnime: (anime: AnimeWithPreferences) -> Unit,
        private val onClickFav: (animeWithPreferences: AnimeWithPreferences) -> Unit,
    ) : PagingDataAdapter<AnimeWithPreferences, AnimeListAdapter.ViewHolder>(AnimeComparator) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding =
                AnimeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position) ?: return
            holder.bind(item, onClickAnime, onClickFav, item.userPreferences?.starred ?: false)
        }
        class ViewHolder(private val binding: AnimeListItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(
                item: AnimeWithPreferences,
                onClickAnime: (anime: AnimeWithPreferences) -> Unit,
                onClickFav: (item: AnimeWithPreferences) -> Unit,
                isStarred: Boolean
            ) {
                with(item.anime) {

                binding.header.headerTitleText.text =
                    this.broadcast.getNextBroadcastString(binding.root.context)
                binding.animeTitle.text = this.title
                binding.description.text = this.synopsis
                Picasso.get().load(this.images.webp.largeImageUrl).into(binding.image)
                val adapter = GenreListAdapter()
                if (binding.genreContainer.itemDecorationCount == 0) binding.genreContainer.addItemDecoration(
                    AnimeListItemDecorator()
                )
                adapter.setData(this.genres)
                binding.genreContainer.adapter = adapter
                binding.header.favoriteButton.setState(isStarred)
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
    }