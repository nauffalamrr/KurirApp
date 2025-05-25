package com.palmar.kurirapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.palmar.kurirapp.data.TripHistory
import com.palmar.kurirapp.databinding.ItemHistoryTripBinding

class TripHistoryAdapter(
    private val tripHistoryList: MutableList<TripHistory>,
    private val onDeleteConfirm: (TripHistory) -> Unit
) : RecyclerView.Adapter<TripHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryTripBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: TripHistory) {
            with(binding) {
                tvHistoryDate.text = history.date
                tvHistoryStatus.text = history.status
                tvHistoryFrom.text = history.from
                tvHistoryDestination1.text = history.destination1

                if (history.destination2.isEmpty()) {
                    layoutDestination2.visibility = View.GONE
                } else {
                    layoutDestination2.visibility = View.VISIBLE
                    tvHistoryDestination2.text = history.destination2
                }

                if (history.destination3.isEmpty()) {
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

                ivDelete.setOnClickListener {
                    onDeleteConfirm(history)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(tripHistoryList[position])
    }

    override fun getItemCount(): Int = tripHistoryList.size
}
