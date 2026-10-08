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
import androidx.recyclerview.widget.LinearLayoutManager
import com.frostfel.animelist.MainActivityViewModel
import com.frostfel.animelist.R
import com.frostfel.animelist.databinding.FilterSheetBinding
import com.frostfel.animelist.databinding.SeasonAnimeFragmentBinding
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.SortOrder
import com.frostfel.animelist.notifications.NotificationPermissionRequest
import com.frostfel.animelist.utils.getQueryFlow
import com.frostfel.animelist.views.season_list.adapter.AnimeListAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
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
    private var items: List<AnimeWithPreferences>? = null
    private var refreshState = RefreshState()
    private var filterSheet: BottomSheetDialog? = null
    // Set when the search, genres or order change; the list jumps to the top once it is updated.
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
        filterSheet?.dismiss()
        filterSheet = null
        binding = null
        items = null
    }

    private fun initView(binding: SeasonAnimeFragmentBinding) {
        binding.recylcerView.layoutManager = LinearLayoutManager(activity)
        binding.recylcerView.adapter = adapter
        // Favourites live only in the local database, there is nothing to pull.
        binding.swipeRefresh.isEnabled = !viewModel.isFav
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.retryButton.setOnClickListener { viewModel.refresh() }
        binding.displayModeButton.setOnClickListener { viewModel.toggleListMode() }
        binding.filterButton.setOnClickListener { showFilterSheet() }

        val scope = viewLifecycleOwner.lifecycleScope
        scope.launch { observeSearch(binding) }
        scope.launch { viewModel.listMode.collect { renderDisplayMode(binding, it) } }
        scope.launch {
            viewModel.hasActiveSheetFilters.collect { active ->
                binding.filterButton.setBackgroundResource(
                    if (active) R.drawable.filter_button_active_background
                    else R.drawable.filter_button_inactive_background
                )
            }
        }
        scope.launch {
            // Changing genres or order (not the first value) moves the list to the top.
            combine(viewModel.selectedGenres, viewModel.sortOrder, ::Pair).drop(1).collect {
                scrollToTopOnUpdate = true
            }
        }
        scope.launch {
            viewModel.animeList.collect { list ->
                adapter.submitList(list) {
                    if (scrollToTopOnUpdate) {
                        scrollToTopOnUpdate = false
                        this@SeasonAnimeFragment.binding?.recylcerView?.scrollToPosition(0)
                    }
                }
                items = list
                render()
            }
        }
        scope.launch {
            viewModel.refreshState.collect {
                refreshState = it
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

    private fun renderDisplayMode(binding: SeasonAnimeFragmentBinding, listMode: Boolean) {
        adapter.listMode = listMode
        // The button shows the mode you switch to.
        binding.displayModeButton.setImageResource(if (listMode) R.drawable.ic_view_cards else R.drawable.ic_view_list)
        binding.displayModeButton.contentDescription =
            getString(if (listMode) R.string.show_as_cards else R.string.show_as_list)
    }

    /** Sort order and genres, applied live while the sheet is open. */
    private fun showFilterSheet() {
        if (filterSheet?.isShowing == true) return
        val sheetBinding = FilterSheetBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(sheetBinding.root)

        sheetBinding.sortPopularity.setOnClickListener { viewModel.setSortOrder(SortOrder.POPULARITY) }
        sheetBinding.sortNextEpisode.setOnClickListener { viewModel.setSortOrder(SortOrder.NEXT_EPISODE) }
        sheetBinding.clearButton.setOnClickListener { viewModel.clearSheetFilters() }

        fun render(genres: List<String>, selected: Set<String>, order: SortOrder) {
            sheetBinding.sortPopularity.isChecked = order == SortOrder.POPULARITY
            sheetBinding.sortNextEpisode.isChecked = order == SortOrder.NEXT_EPISODE
            renderGenres(sheetBinding, genres, selected)
        }
        // Fill it before showing, so it opens at its final size instead of growing.
        render(viewModel.genres.value, viewModel.selectedGenres.value, viewModel.sortOrder.value)
        val job = viewLifecycleOwner.lifecycleScope.launch {
            combine(viewModel.genres, viewModel.selectedGenres, viewModel.sortOrder, ::Triple)
                .collect { (genres, selected, order) -> render(genres, selected, order) }
        }
        dialog.setOnDismissListener {
            job.cancel()
            filterSheet = null
        }
        dialog.behavior.skipCollapsed = true
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        filterSheet = dialog
        dialog.show()
    }

    private fun renderGenres(sheet: FilterSheetBinding, genres: List<String>, selected: Set<String>) {
        // Keep selected genres visible even if they are no longer in the list.
        val names = selected.filter { it !in genres }.sorted() + genres
        sheet.genresTitle.isVisible = names.isNotEmpty()
        val group = sheet.genreFilter
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
        val items = items
        val isEmpty = items.isNullOrEmpty()
        val refreshing = refreshState.refreshing
        val error = refreshState.error

        binding.loading.isVisible = items == null || (isEmpty && refreshing)
        binding.swipeRefresh.isRefreshing = !isEmpty && refreshing

        val message = when {
            items == null || !isEmpty || refreshing -> null
            viewModel.hasActiveFilter -> R.string.search_no_results
            error != null -> R.string.season_load_error
            viewModel.isFav -> R.string.favorites_empty
            else -> null
        }
        binding.messageContainer.isVisible = message != null
        message?.let { binding.messageText.setText(it) }
        binding.retryButton.isVisible = message == R.string.season_load_error

        // Data is still there from the cache: just let the user know it may be stale.
        if (!isEmpty && error != null && error !== lastShownError) {
            lastShownError = error
            Toast.makeText(requireContext(), R.string.offline_showing_cached, Toast.LENGTH_SHORT).show()
        }
    }
}
