package com.palmar.kurirapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.palmar.kurirapp.data.SimpleLocation
import com.palmar.kurirapp.databinding.ItemResultBinding

class ResultAdapter(
    private val orderedDestinations: List<SimpleLocation>
) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(val binding: ItemResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val dest = orderedDestinations[position]

        holder.binding.tvDestinationNumber.text = "Route ${position + 1}"

        holder.binding.tvDestination.text = if(dest.name.isNotEmpty()) {
            dest.name
        } else {
            "Pilih lokasi"
        }
    }

    override fun getItemCount(): Int = orderedDestinations.size
}
