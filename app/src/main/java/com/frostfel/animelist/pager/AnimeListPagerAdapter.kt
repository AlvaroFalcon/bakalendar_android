package com.frostfel.animelist.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.frostfel.animelist.views.season_list.SeasonAnimeFragment

class AnimeListPagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return SeasonAnimeFragment.newInstance(position == 1)
    }
}