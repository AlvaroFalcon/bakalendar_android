package com.frostfel.animelist.views.anime_detail

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.databinding.RelatedItemBinding
import com.frostfel.animelist.model.RelatedItem
import com.frostfel.animelist.views.utils.loadCached

/** Horizontal carousel of related anime and manga; the relation label uses [accent]. */
class RelatedAdapter(
    private val onClick: (RelatedItem) -> Unit,
) : ListAdapter<RelatedItem, RelatedAdapter.ViewHolder>(Comparator) {

    @ColorInt
    var accent: Int = 0
        @SuppressLint("NotifyDataSetChanged") // only the label colour changes
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(RelatedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            name.text = item.name
            relation.text = item.relation
            relation.backgroundTintList = ColorStateList.valueOf(accent)
            mediaType.text = item.mediaType
            cover.setImageDrawable(null)
            cover.loadCached(item.imageUrl)
            root.contentDescription = "${item.relation}: ${item.name}"
            root.setOnClickListener { onClick(item) }
        }
    }

    class ViewHolder(val binding: RelatedItemBinding) : RecyclerView.ViewHolder(binding.root)

    private object Comparator : DiffUtil.ItemCallback<RelatedItem>() {
        override fun areItemsTheSame(oldItem: RelatedItem, newItem: RelatedItem) =
            oldItem.malId == newItem.malId && oldItem.isAnime == newItem.isAnime

        override fun areContentsTheSame(oldItem: RelatedItem, newItem: RelatedItem) = oldItem == newItem
    }
}
