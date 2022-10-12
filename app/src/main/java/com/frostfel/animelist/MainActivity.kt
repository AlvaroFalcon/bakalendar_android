package com.frostfel.animelist

import android.content.Intent
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
import com.frostfel.animelist.views.anime_detail.AnimeDetailActivity
import com.frostfel.animelist.views.anime_detail.AnimeDetailFragment
import com.frostfel.animelist.views.season_list.SeasonAnimeFragment
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AnimeListNavigation {
    private val viewModel by viewModels<MainActivityViewModel>()
    private lateinit var binding: ActivityMainBinding
    private val adapter = AnimeListPagerAdapter(this)
    private lateinit var tabTitles : Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
        viewModel.initViewModel(this)
        navigateToAnimeListFragment()
    }

    private fun initView() {
        tabTitles = arrayOf(
            resources.getString(R.string.all_tab_title),
            resources.getString(R.string.favorites_tab_title)
        )
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
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
        val detailIntent = Intent(this, AnimeDetailActivity::class.java)
        val bundle = Bundle().apply { putParcelable(AnimeDetailActivity.ANIME_EXTRA, anime) }
        detailIntent.putExtras(bundle)
        startActivity(detailIntent)
    }
}