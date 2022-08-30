package com.frostfel.animelist.views.season_list

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.frostfel.animelist.MainActivityViewModel
import com.frostfel.animelist.databinding.SeasonAnimeFragmentBinding
import com.frostfel.animelist.views.season_list.adapter.AnimeListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SeasonAnimeFragment : Fragment() {
    private val viewModel by viewModels<SeasonAnimeViewModel>()
    private val activityViewModel by activityViewModels<MainActivityViewModel>()
    private lateinit var binding: SeasonAnimeFragmentBinding
    private val adapter = AnimeListAdapter { anime ->
        activityViewModel.navigator.navigateToAnimeDetail(anime)

    }

    private val handler = Handler(Looper.getMainLooper())

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
        binding.searchView.setOnFocusChangeListener { _, focused ->
            binding.searchView.gravity = when (focused) {
                true -> {
                    Gravity.START
                }
                else -> {
                    Gravity.CENTER
                }
            }
        }

        binding.searchView.addTextChangedListener {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                it?.let {
                    viewModel.filterText.postValue(it.trim().toString())
                } ?: viewModel.filterText.postValue("")

            }, 800)
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.filterText.observe(viewLifecycleOwner){
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