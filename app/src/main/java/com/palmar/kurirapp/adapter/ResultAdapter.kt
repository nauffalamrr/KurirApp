package com.palmar.kurirapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.palmar.kurirapp.data.Result
import com.palmar.kurirapp.databinding.ItemResultBinding

class ResultAdapter(
    private val results: List<Result>
) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    inner class ResultViewHolder(val binding: ItemResultBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        holder.binding.tvDestinationNumber.text = "Route ${result.number}"
        holder.binding.tvDestination.text = result.destination
    }

    override fun getItemCount() = results.size
}
