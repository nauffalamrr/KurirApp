package com.palmar.kurirapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.palmar.kurirapp.R
import com.palmar.kurirapp.data.TripHistory
import com.palmar.kurirapp.databinding.ItemHistoryTripBinding
import com.palmar.kurirapp.helper.DateHelper

class TripHistoryAdapter(
    private val tripHistoryList: MutableList<TripHistory>
) : RecyclerView.Adapter<TripHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemHistoryTripBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: TripHistory) {
            with(binding) {
                tvHistoryDate.text = DateHelper.formatDate(history.created_at)
                tvHistoryStatus.text = history.status

                layoutDestinationsContainer.removeAllViews()

                history.destinations.forEachIndexed { index, destination ->
                    val container = LinearLayout(binding.root.context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        val paddingPx = (8 * binding.root.resources.displayMetrics.density).toInt()
                        setPadding(0, paddingPx, 0, paddingPx)
                    }

                    val icon = ImageView(binding.root.context).apply {
                        setImageResource(R.drawable.ic_destination)
                        val sizePx = (16 * binding.root.resources.displayMetrics.density).toInt()
                        layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                            marginEnd = (8 * binding.root.resources.displayMetrics.density).toInt()
                        }
                    }

                    val tv = TextView(binding.root.context).apply {
                        text = "${destination.destination_name}"
                        textSize = 12f
                    }

                    container.addView(icon)
                    container.addView(tv)
                    layoutDestinationsContainer.addView(container)
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
