package com.palmar.kurirapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.palmar.kurirapp.data.Destination
import com.palmar.kurirapp.databinding.ItemDetailTaskBinding

class DetailTaskAdapter(
    private var destinations: List<Destination>
) : RecyclerView.Adapter<DetailTaskAdapter.DestinationViewHolder>() {

    inner class DestinationViewHolder(private val binding: ItemDetailTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(destination: Destination) {
            binding.taskName.text = destination.location.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ItemDetailTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DestinationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position])
    }

    override fun getItemCount(): Int = destinations.size

    fun updateData(newDestinations: List<Destination>) {
        destinations = newDestinations
        notifyDataSetChanged()
    }
}

