package com.frostfel.animelist.season_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.frostfel.animelist.databinding.SeasonAnimeFragmentBinding
import com.frostfel.animelist.season_list.adapter.AnimeListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SeasonAnimeFragment : Fragment() {
    private val viewModel by viewModels<SeasonAnimeViewModel>()
    private lateinit var binding: SeasonAnimeFragmentBinding
    private val adapter = AnimeListAdapter()

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
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.retrieveData().observe(viewLifecycleOwner) {
                it?.let {
                    adapter.submitData(lifecycle, it)
                }
            }
        }
    }
}