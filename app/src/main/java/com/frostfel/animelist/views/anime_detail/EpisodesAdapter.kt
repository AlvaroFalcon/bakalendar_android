package com.frostfel.animelist.views.anime_detail

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.R
import com.frostfel.animelist.databinding.EpisodeItemBinding
import com.frostfel.animelist.model.EpisodeItem
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Aired episodes are tappable; upcoming ones are faded and show the expected date. */
class EpisodesAdapter(
    private val onClick: (EpisodeItem) -> Unit,
) : ListAdapter<EpisodeItem, EpisodesAdapter.ViewHolder>(Comparator) {

    @ColorInt
    var accent: Int = 0
        @SuppressLint("NotifyDataSetChanged") // only colours change
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    private val airedFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val expectedFormat = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(EpisodeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = getItem(position)
        val context = holder.itemView.context
        with(holder.binding) {
            number.text = episode.number.toString()
            number.setTextColor(accent)
            numberLabel.setTextColor(accent)
            numberBox.backgroundTintList = ColorStateList.valueOf(ColorUtils.blendARGB(accent, Color.WHITE, 0.86f))
            title.text = episode.title ?: context.getString(R.string.episode_untitled, episode.number)

            // Aired dates come as a day (JST); expected ones are exact slots in the user's zone.
            val date = episode.date?.let {
                if (episode.aired) {
                    it.toLocalDate().format(airedFormat)
                } else {
                    context.getString(
                        R.string.episode_expected,
                        it.withZoneSameInstant(ZoneId.systemDefault()).format(expectedFormat)
                    )
                }
            }
            val badges = listOfNotNull(
                context.getString(R.string.episode_filler).takeIf { episode.filler },
                context.getString(R.string.episode_recap).takeIf { episode.recap },
            )
            subtitle.text = (listOfNotNull(date) + badges).joinToString(" · ")
            subtitle.isVisible = subtitle.text.isNotEmpty()

            play.isVisible = episode.aired
            play.backgroundTintList = ColorStateList.valueOf(accent)
            root.alpha = if (episode.aired) 1f else UPCOMING_ALPHA
            // setOnClickListener makes the view clickable again, so it goes first.
            root.setOnClickListener(if (episode.aired) ({ onClick(episode) }) else null)
            root.isClickable = episode.aired
            root.isFocusable = episode.aired
        }
    }

    class ViewHolder(val binding: EpisodeItemBinding) : RecyclerView.ViewHolder(binding.root)

    private object Comparator : DiffUtil.ItemCallback<EpisodeItem>() {
        override fun areItemsTheSame(oldItem: EpisodeItem, newItem: EpisodeItem) = oldItem.number == newItem.number
        override fun areContentsTheSame(oldItem: EpisodeItem, newItem: EpisodeItem) = oldItem == newItem
    }

    companion object {
        const val UPCOMING_ALPHA = 0.45f
    }
}
