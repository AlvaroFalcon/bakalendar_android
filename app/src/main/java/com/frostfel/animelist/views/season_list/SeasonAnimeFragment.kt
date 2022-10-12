package com.frostfel.animelist.views.season_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.frostfel.animelist.MainActivityViewModel
import com.frostfel.animelist.databinding.SeasonAnimeFragmentBinding
import com.frostfel.animelist.utils.getQueryFlow
import com.frostfel.animelist.views.season_list.adapter.AnimeListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@AndroidEntryPoint
open class SeasonAnimeFragment : Fragment() {
    private val viewModel by viewModels<SeasonAnimeViewModel>()
    private val activityViewModel by activityViewModels<MainActivityViewModel>()
    private lateinit var binding: SeasonAnimeFragmentBinding
    private val adapter = AnimeListAdapter({ anime ->
        activityViewModel.navigator.navigateToAnimeDetail(anime)
    }, {
        viewModel.onFavTap(it) {
            setTextQuery(binding.searchView.getQuery())
        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.isFav = it.getBoolean(IS_FAV_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        observeData()
        binding = SeasonAnimeFragmentBinding.inflate(inflater, container, false)
        initView(binding)
        return binding.root
    }

    private fun initView(binding: SeasonAnimeFragmentBinding) {
        binding.recylcerView.layoutManager = LinearLayoutManager(activity)
        binding.recylcerView.adapter = adapter
        viewModel.favAnime.observe(viewLifecycleOwner) {
            adapter.updateFavs(it)
        }
        setupSearchFlow(binding)
    }

    private fun setupSearchFlow(binding: SeasonAnimeFragmentBinding) {
        lifecycleScope.launch {
            binding.searchView.getQueryFlow()
                .drop(1)
                .debounce(200L)
                .distinctUntilChanged()
                .collect {
                    viewModel.filterText.postValue(it)
                }
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.filterText.observe(viewLifecycleOwner) {
                setTextQuery(it)
            }
        }
    }

    private fun showContent() {
        activity?.runOnUiThread {
            binding.loading.isVisible = false
            binding.resultContainer.visibility = View.VISIBLE
        }
    }

    private fun setTextQuery(query: String) {
        lifecycleScope.launch {
            viewModel.retrieveData(query).collectLatest {
                showContent()
                adapter.submitData(it)
            }
        }
    }
}