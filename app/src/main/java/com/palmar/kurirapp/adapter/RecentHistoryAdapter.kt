package com.palmar.kurirapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.palmar.kurirapp.data.TripHistory
import com.palmar.kurirapp.databinding.ItemRecentHistoryBinding

class RecentHistoryAdapter(
    private val tripHistoryList: MutableList<TripHistory>
) : RecyclerView.Adapter<RecentHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemRecentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: TripHistory) {
            with(binding) {
                tvHistoryDate.text = history.date
                tvHistoryStatus.text = history.status
                tvHistoryFrom.text = history.from
                tvHistoryDestination1.text = history.destination1

                if (history.destination2.isNullOrEmpty()) {
                    layoutDestination2.visibility = View.GONE
                } else {
                    layoutDestination2.visibility = View.VISIBLE
                    tvHistoryDestination2.text = history.destination2
                }

                if (history.destination3.isNullOrEmpty()) {
                    layoutDestination3.visibility = View.GONE
                } else {
                    layoutDestination3.visibility = View.VISIBLE
                    tvHistoryDestination3.text = history.destination3
                }

                val vehicleIconRes = when (history.vehicle.lowercase()) {
                    "motorcycle" -> com.palmar.kurirapp.R.drawable.ic_motor
                    "car" -> com.palmar.kurirapp.R.drawable.ic_car
                    else -> com.palmar.kurirapp.R.drawable.ic_motor
                }
                ivVehicle.setImageResource(vehicleIconRes)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(tripHistoryList[position])
    }

    override fun getItemCount(): Int = tripHistoryList.size

    fun updateData(newData: List<TripHistory>) {
        tripHistoryList.clear()
        tripHistoryList.addAll(newData)
        notifyDataSetChanged()
    }
}
