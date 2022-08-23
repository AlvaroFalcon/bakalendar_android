package com.frostfel.animelist.season_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.frostfel.animelist.databinding.SeasonAnimeFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeasonAnimeFragment : Fragment() {
    private val viewModel by viewModels<SeasonAnimeViewModel>()
    private lateinit var binding: SeasonAnimeFragmentBinding

    companion object {
        fun newInstance() = SeasonAnimeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        context?.let { viewModel.getData(it) }
        binding = SeasonAnimeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

}