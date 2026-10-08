package com.frostfel.animelist.views.calendar

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.R
import com.frostfel.animelist.databinding.CalendarDayPageBinding
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.CalendarEntry
import java.time.DayOfWeek

/** One page per weekday, Monday to Sunday. */
class CalendarPagerAdapter(
    private val onClickAnime: (AnimeWithPreferences) -> Unit,
    private val onClickFav: (AnimeWithPreferences) -> Unit,
) : RecyclerView.Adapter<CalendarPagerAdapter.PageHolder>() {

    private var week: Map<DayOfWeek, List<CalendarEntry>> = emptyMap()
    private var favouritesOnly = false

    @SuppressLint("NotifyDataSetChanged") // seven pages, each diffs its own list
    fun submit(week: Map<DayOfWeek, List<CalendarEntry>>, favouritesOnly: Boolean) {
        this.week = week
        this.favouritesOnly = favouritesOnly
        notifyDataSetChanged()
    }

    override fun getItemCount() = DAYS.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        val binding = CalendarDayPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val dayAdapter = CalendarDayAdapter(onClickAnime, onClickFav)
        binding.dayList.layoutManager = LinearLayoutManager(parent.context)
        binding.dayList.adapter = dayAdapter
        return PageHolder(binding, dayAdapter)
    }

    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        val entries = week[DAYS[position]].orEmpty()
        holder.adapter.submitList(entries)
        holder.binding.emptyText.isVisible = entries.isEmpty()
        holder.binding.emptyText.setText(
            if (favouritesOnly) R.string.calendar_empty_day_favorites else R.string.calendar_empty_day
        )
    }

    class PageHolder(val binding: CalendarDayPageBinding, val adapter: CalendarDayAdapter) :
        RecyclerView.ViewHolder(binding.root)

    companion object {
        val DAYS: List<DayOfWeek> = DayOfWeek.values().toList()
    }
}
