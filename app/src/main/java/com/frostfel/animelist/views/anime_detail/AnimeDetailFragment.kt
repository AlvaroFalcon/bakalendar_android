package com.frostfel.animelist.views.anime_detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.frostfel.animelist.databinding.FragmentAnimeDetailBinding
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.model.getNextBroadcastString
import com.frostfel.animelist.views.season_list.adapter.AnimeListAdapter
import com.frostfel.animelist.views.season_list.adapter.GenreListAdapter
import com.frostfel.animelist.views.season_list.decorator.AnimeListItemDecorator
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

private const val ANIME_PARAM = "ANIME_PARAM"

@AndroidEntryPoint
class AnimeDetailFragment : Fragment() {
    private val viewModel by viewModels<AnimeDetailFragmentViewModel>()
    private lateinit var binding: FragmentAnimeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.anime.postValue(it.getParcelable(ANIME_PARAM))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnimeDetailBinding.inflate(inflater, container, false)
        observeData()
        return binding.root
    }

    private fun observeData() {
        viewModel.anime.observe(viewLifecycleOwner) {
            setupView(it)
        }
    }

    private fun setupView(anime: Anime) {
        with(binding) {
            Picasso.get().load(anime.images.webp.largeImageUrl).into(image)
            context?.let { header.headerTitleText.text = anime.broadcast.getNextBroadcastString(it) }
            animeTitle.text = anime.title
            description.text = anime.synopsis
            val adapter = GenreListAdapter()
            if (binding.genreContainer.itemDecorationCount == 0) binding.genreContainer.addItemDecoration(
                AnimeListItemDecorator()
            )
            adapter.setData(anime.genres)
            binding.genreContainer.adapter = adapter
        }
    }

    companion object {
        fun newInstance(anime: Anime) =
            AnimeDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ANIME_PARAM, anime)
                }
            }
    }
}