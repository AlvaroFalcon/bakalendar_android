package com.frostfel.animelist.views.calendar

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.frostfel.animelist.databinding.ActivityCalendarBinding
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.notifications.NotificationPermissionRequest
import com.frostfel.animelist.views.anime_detail.AnimeDetailActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** Monday to Sunday, with what airs each day in the user's timezone. */
@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {
    private val viewModel by viewModels<CalendarViewModel>()
    private val notificationPermission = NotificationPermissionRequest(this)
    private lateinit var binding: ActivityCalendarBinding

    private val pagerAdapter = CalendarPagerAdapter(
        onClickAnime = ::openDetail,
        onClickFav = { if (viewModel.onFavTap(it)) notificationPermission.requestIfNeeded() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }
        binding.dayPager.adapter = pagerAdapter
        TabLayoutMediator(binding.dayTabs, binding.dayPager) { tab, position ->
            tab.text = CalendarPagerAdapter.DAYS[position].getDisplayName(TextStyle.SHORT, Locale.getDefault())
        }.attach()
        if (savedInstanceState == null) {
            val today = CalendarPagerAdapter.DAYS.indexOf(LocalDate.now().dayOfWeek)
            binding.dayPager.setCurrentItem(today, false)
        }

        binding.favoritesOnly.setOnClickListener { viewModel.setFavouritesOnly(binding.favoritesOnly.isChecked) }
        lifecycleScope.launch {
            combine(viewModel.week, viewModel.favouritesOnly, ::Pair).collect { (week, favouritesOnly) ->
                binding.favoritesOnly.isChecked = favouritesOnly
                pagerAdapter.submit(week, favouritesOnly)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun openDetail(anime: AnimeWithPreferences) {
        startActivity(AnimeDetailActivity.intent(this, anime.anime.malId))
    }
}
