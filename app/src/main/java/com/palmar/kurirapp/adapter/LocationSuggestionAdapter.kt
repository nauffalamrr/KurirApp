package com.palmar.kurirapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.palmar.kurirapp.R
import com.palmar.kurirapp.data.SimpleLocation

class LocationSuggestionAdapter(private val onClickListener: (SimpleLocation) -> Unit) :
    RecyclerView.Adapter<LocationSuggestionAdapter.ViewHolder>() {

    private var suggestions: List<SimpleLocation> = emptyList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationNameTextView: TextView = itemView.findViewById(R.id.text_view_location_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.locationNameTextView.text = suggestion.name
        holder.itemView.setOnClickListener { onClickListener(suggestion) }
    }

    override fun getItemCount(): Int = suggestions.size

    fun updateSuggestions(newSuggestions: List<SimpleLocation>) {
        suggestions = newSuggestions
        notifyDataSetChanged()
    }
}
