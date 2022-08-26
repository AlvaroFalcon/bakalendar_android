package com.frostfel.animelist.views.season_list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frostfel.animelist.databinding.GenreListItemBinding
import com.frostfel.animelist.model.GenericMalData

class GenreListAdapter: RecyclerView.Adapter<GenreListAdapter.ViewHolder>() {
    private val genreList = mutableListOf<GenericMalData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = GenreListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(genreList[position])
    }

    override fun getItemCount(): Int {
        return genreList.size
    }

    fun setData(items: List<GenericMalData>) {
        genreList.clear()
        genreList.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: GenreListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(genericMalData: GenericMalData) {
            binding.text.text = genericMalData.name
        }
    }
}