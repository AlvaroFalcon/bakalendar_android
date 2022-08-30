package com.frostfel.animelist.views.season_list

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
class SeasonAnimeFragment : Fragment() {
    private val viewModel by viewModels<SeasonAnimeViewModel>()
    private val activityViewModel by activityViewModels<MainActivityViewModel>()
    private lateinit var binding: SeasonAnimeFragmentBinding
    private val adapter = AnimeListAdapter { anime ->
        activityViewModel.navigator.navigateToAnimeDetail(anime)

    }

    companion object {
        fun newInstance() = SeasonAnimeFragment()
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
        setupSearchFlow(binding)
    }

    private fun setupSearchFlow(binding: SeasonAnimeFragmentBinding) {
        lifecycleScope.launch {
            binding.searchView.getQueryFlow()
                .drop(1)
                .debounce(300L)
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

    private fun setTextQuery(query: String) {
        lifecycleScope.launch {
            viewModel.retrieveData(query).collectLatest {
                adapter.submitData(it)
            }
        }
    }
}