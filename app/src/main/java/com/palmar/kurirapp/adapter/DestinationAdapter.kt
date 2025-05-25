package com.palmar.kurirapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.palmar.kurirapp.data.Destination
import com.palmar.kurirapp.databinding.ItemAddDestinationBinding

class DestinationAdapter(
    private var destinations: MutableList<Destination>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    inner class DestinationViewHolder(private val binding: ItemAddDestinationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(destination: Destination) {
            binding.tvDestination.text = "Your Destination"
            binding.tvDestinationDetail.text = destination.location.name.ifEmpty { "Pilih lokasi" }

            binding.root.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ItemAddDestinationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DestinationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position])
    }

    override fun getItemCount(): Int = destinations.size

    fun updateData(newDestinations: MutableList<Destination>) {
        destinations = newDestinations
        notifyDataSetChanged()
    }
}