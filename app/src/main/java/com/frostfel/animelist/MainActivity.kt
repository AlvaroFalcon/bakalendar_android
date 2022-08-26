package com.frostfel.animelist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.views.anime_detail.AnimeDetailFragment
import com.frostfel.animelist.views.season_list.SeasonAnimeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AnimeListNavigation {
    private val viewModel by viewModels<MainActivityViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel.initViewModel(this)
        navigateToAnimeListFragment()
    }

    override fun navigateToAnimeListFragment() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, SeasonAnimeFragment.newInstance())
        }
    }

    override fun navigateToAnimeDetail(anime: Anime) {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, AnimeDetailFragment.newInstance(anime))
                .addToBackStack(AnimeDetailFragment::class.simpleName)
        }
    }
}