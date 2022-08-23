package com.frostfel.animelist.season_list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.databinding.AnimeListItemBinding
import com.frostfel.animelist.model.Anime

class AnimeListAdapter() : RecyclerView.Adapter<AnimeListAdapter.ViewHolder>() {
    private val animeData: MutableList<Anime> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeListAdapter.ViewHolder {
        val binding =
            AnimeListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimeListAdapter.ViewHolder, position: Int) =
        holder.bind(animeData[position])

    override fun getItemCount(): Int {
        return animeData.size
    }

    fun setData(data: List<Anime>) {
        if(animeData.isNotEmpty()) return
        this.animeData.addAll(data)
        println("ALVARO COUNT ${data.size}")
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: AnimeListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(anime: Anime) {
            binding.text.text = anime.title
        }
    }
}