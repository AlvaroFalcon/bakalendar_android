package com.frostfel.animelist.views.anime_detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.frostfel.animelist.databinding.FragmentAnimeDetailBinding
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.getNextBroadcastString
import com.frostfel.animelist.notifications.NotificationPermissionRequest
import com.frostfel.animelist.views.season_list.adapter.GenreListAdapter
import com.frostfel.animelist.views.season_list.decorator.AnimeListItemDecorator
import com.frostfel.animelist.views.utils.loadCached
import dagger.hilt.android.AndroidEntryPoint

private const val ANIME_PARAM = "ANIME_PARAM"

@AndroidEntryPoint
class AnimeDetailFragment : Fragment() {
    private val viewModel by viewModels<AnimeDetailFragmentViewModel>()
    private val notificationPermission = NotificationPermissionRequest(this)
    private lateinit var binding: FragmentAnimeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.setAnimeId(it.getParcelable<AnimeWithPreferences>(ANIME_PARAM)?.anime?.malId ?: 0)
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
            it ?: return@observe
            setupView(it)
        }
    }

    private fun setupView(item: AnimeWithPreferences) {
        with(binding) {
            image.loadCached(item.anime.images.webp.largeImageUrl)
            context?.let { header.headerTitleText.text = item.anime.getNextBroadcastString(it) }
            animeTitle.text = item.anime.title
            description.text = item.anime.synopsis
            val adapter = GenreListAdapter()
            if (binding.genreContainer.itemDecorationCount == 0) binding.genreContainer.addItemDecoration(
                AnimeListItemDecorator()
            )
            adapter.setData(item.anime.genres)
            binding.genreContainer.adapter = adapter
            header.favoriteButton.setState(item.userPreferences?.starred ?: false)
            header.favoriteButton.setOnClickListener {
                if (viewModel.onFavTap(item)) notificationPermission.requestIfNeeded()
            }
        }
    }

    companion object {
        fun newInstance(anime: AnimeWithPreferences?) =
            AnimeDetailFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ANIME_PARAM, anime)
                }
            }
    }
}