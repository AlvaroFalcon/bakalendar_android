package com.frostfel.animelist.views.season_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.frostfel.animelist.MainActivityViewModel
import com.frostfel.animelist.R
import com.frostfel.animelist.databinding.SeasonAnimeFragmentBinding
import com.frostfel.animelist.notifications.NotificationPermissionRequest
import com.frostfel.animelist.utils.getQueryFlow
import com.frostfel.animelist.views.season_list.adapter.AnimeListAdapter
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@AndroidEntryPoint
open class SeasonAnimeFragment : Fragment() {
    private val viewModel by viewModels<SeasonAnimeViewModel>()
    private val activityViewModel by activityViewModels<MainActivityViewModel>()
    private val notificationPermission = NotificationPermissionRequest(this)
    private var binding: SeasonAnimeFragmentBinding? = null
    private var lastShownError: Throwable? = null
    private var loadStates: CombinedLoadStates? = null
    private var lastSelectedGenres: Set<String>? = null
    // Set when the search or genre filter changes; the list jumps to the top once it is updated.
    private var scrollToTopOnUpdate = false
    private val adapter = AnimeListAdapter({ item ->
        activityViewModel.navigator.navigateToAnimeDetail(item)
    }, {
        if (viewModel.onFavTap(it)) notificationPermission.requestIfNeeded()
    })

    companion object {
        const val IS_FAV_PARAM = "IS_FAV_PARAM"
        fun newInstance(isFav: Boolean) =
            SeasonAnimeFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(IS_FAV_PARAM, isFav)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = SeasonAnimeFragmentBinding.inflate(inflater, container, false)
        this.binding = binding
        initView(binding)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        lastSelectedGenres = null
    }

    private fun initView(binding: SeasonAnimeFragmentBinding) {
        binding.recylcerView.layoutManager = LinearLayoutManager(activity)
        binding.recylcerView.adapter = adapter
        // Favourites live only in the local database, there is nothing to pull.
        binding.swipeRefresh.isEnabled = !viewModel.isFav
        binding.swipeRefresh.setOnRefreshListener { adapter.refresh() }
        binding.retryButton.setOnClickListener { adapter.refresh() }

        val scope = viewLifecycleOwner.lifecycleScope
        scope.launch { observeSearch(binding) }
        scope.launch {
            combine(viewModel.genres, viewModel.selectedGenres, ::Pair).collect { (genres, selected) ->
                if (lastSelectedGenres != null && lastSelectedGenres != selected) scrollToTopOnUpdate = true
                lastSelectedGenres = selected
                renderGenres(binding, genres, selected)
            }
        }
        scope.launch { viewModel.animeList.collectLatest { adapter.submitData(it) } }
        scope.launch {
            adapter.loadStateFlow.collect {
                loadStates = it
                render()
            }
        }
        // Load states do not change when a favourite is removed or a search filters everything out.
        scope.launch {
            adapter.onPagesUpdatedFlow.collect {
                if (scrollToTopOnUpdate) {
                    scrollToTopOnUpdate = false
                    binding.recylcerView.scrollToPosition(0)
                }
                render()
            }
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun observeSearch(binding: SeasonAnimeFragmentBinding) {
        binding.searchView.getQueryFlow()
            .drop(1)
            .debounce(200L)
            .distinctUntilChanged()
            .collect {
                scrollToTopOnUpdate = true
                viewModel.setQuery(it)
            }
    }

    private fun renderGenres(binding: SeasonAnimeFragmentBinding, genres: List<String>, selected: Set<String>) {
        // Keep selected genres visible even if they are no longer in the list.
        val names = selected.filter { it !in genres }.sorted() + genres
        binding.genreFilterScroll.isVisible = names.isNotEmpty()
        val group = binding.genreFilter
        val current = (0 until group.childCount).map { (group.getChildAt(it) as Chip).text.toString() }
        if (current != names) {
            group.removeAllViews()
            names.forEach { name ->
                val chip = layoutInflater.inflate(R.layout.genre_filter_chip, group, false) as Chip
                chip.text = name
                chip.setOnClickListener { viewModel.onGenreTap(name) }
                group.addView(chip)
            }
        }
        (0 until group.childCount).forEach {
            val chip = group.getChildAt(it) as Chip
            chip.isChecked = chip.text.toString() in selected
        }
    }

    private fun render() {
        val binding = binding ?: return
        val loadStates = loadStates ?: return
        val remoteRefresh = loadStates.mediator?.refresh
        val isRemoteLoading = remoteRefresh is LoadState.Loading
        val remoteError = (remoteRefresh as? LoadState.Error)?.error
        val isLocalLoading = loadStates.source.refresh is LoadState.Loading
        val isEmpty = adapter.itemCount == 0

        binding.loading.isVisible = isEmpty && (isRemoteLoading || isLocalLoading)
        binding.swipeRefresh.isRefreshing = !isEmpty && isRemoteLoading

        val message = when {
            !isEmpty || isRemoteLoading || isLocalLoading -> null
            remoteError != null -> R.string.season_load_error
            viewModel.hasActiveFilter -> R.string.search_no_results
            viewModel.isFav -> R.string.favorites_empty
            else -> null
        }
        binding.messageContainer.isVisible = message != null
        message?.let { binding.messageText.setText(it) }
        binding.retryButton.isVisible = message == R.string.season_load_error

        // Data is still there from the cache: just let the user know it may be stale.
        if (!isEmpty && remoteError != null && remoteError !== lastShownError) {
            lastShownError = remoteError
            Toast.makeText(requireContext(), R.string.offline_showing_cached, Toast.LENGTH_SHORT).show()
        }
    }
}
