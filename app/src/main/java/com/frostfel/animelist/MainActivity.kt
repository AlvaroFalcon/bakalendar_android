package com.frostfel.animelist

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.commit
import com.frostfel.animelist.databinding.ActivityMainBinding
import com.frostfel.animelist.model.Anime
import com.frostfel.animelist.pager.AnimeListPagerAdapter
import com.frostfel.animelist.views.anime_detail.AnimeDetailFragment
import com.frostfel.animelist.views.season_list.SeasonAnimeFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AnimeListNavigation {
    private val viewModel by viewModels<MainActivityViewModel>()
    private lateinit var binding: ActivityMainBinding
    private val adapter = AnimeListPagerAdapter(this)
    private val TAB_TITLES = arrayOf("All", "Favorites")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.pager) {tab, position ->
            tab.text = TAB_TITLES[position]
        }.attach()
        viewModel.initViewModel(this)
        navigateToAnimeListFragment()
    }

    override fun navigateToAnimeListFragment() {
        supportFragmentManager.commit {
            val fragment = SeasonAnimeFragment.newInstance(true)
            //replace(R.id.fragmentContainer, fragment)
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun navigateToAnimeDetail(anime: Anime) {
        val fragment = AnimeDetailFragment.newInstance(anime)

        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            //add(R.id.fragmentContainer, fragment)
            //addToBackStack(AnimeDetailFragment::class.simpleName)
        }
    }
}