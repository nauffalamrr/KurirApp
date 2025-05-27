package com.palmar.kurirapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.palmar.kurirapp.R
import com.palmar.kurirapp.data.TripHistory
import com.palmar.kurirapp.databinding.ItemRecentHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class RecentHistoryAdapter(
    private val originalList: List<TripHistory>
) : RecyclerView.Adapter<RecentHistoryAdapter.ViewHolder>() {

    private val tripHistoryList = mutableListOf<TripHistory>()

    inner class ViewHolder(private val binding: ItemRecentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: TripHistory) {
            with(binding) {
                tvHistoryDate.text = formatDate(history.created_at)
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

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                val outputFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                if (date != null) outputFormat.format(date) else dateString
            } catch (e: Exception) {
                dateString
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

    fun filterByDriverId(userId: Int) {
        val filtered = originalList.filter { it.driver_id == userId }
        updateData(filtered)
    }
}
