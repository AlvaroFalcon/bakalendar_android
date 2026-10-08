package com.frostfel.animelist.views.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.databinding.AnimeListRowBinding
import com.frostfel.animelist.model.AnimeWithPreferences
import com.frostfel.animelist.model.CalendarEntry
import com.frostfel.animelist.views.season_list.adapter.AnimeRowBinder
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** The anime airing on one weekday, with their local time. */
class CalendarDayAdapter(
    private val onClickAnime: (AnimeWithPreferences) -> Unit,
    private val onClickFav: (AnimeWithPreferences) -> Unit,
) : ListAdapter<CalendarEntry, CalendarDayAdapter.ViewHolder>(EntryComparator) {

    private val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        AnimeListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        AnimeRowBinder.bind(
            holder.binding, entry.item, entry.airsAt.format(timeFormat), onClickAnime, onClickFav
        )
    }

    class ViewHolder(val binding: AnimeListRowBinding) : RecyclerView.ViewHolder(binding.root)

    private object EntryComparator : DiffUtil.ItemCallback<CalendarEntry>() {
        override fun areItemsTheSame(oldItem: CalendarEntry, newItem: CalendarEntry) =
            oldItem.item.anime.malId == newItem.item.anime.malId

        override fun areContentsTheSame(oldItem: CalendarEntry, newItem: CalendarEntry) =
            oldItem == newItem
    }
}
