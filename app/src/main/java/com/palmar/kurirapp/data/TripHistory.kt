package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DestinationHistory(
    val id: Int,
    val task_id: Int,
    val destination_name: String,
    val latitude: Double?,
    val longitude: Double?,
    val sequence_order: Int,
    val created_at: String,
    val updated_at: String
) : Parcelable

@Parcelize
data class TripHistory(
    val id: Int,
    val driver_id: Int,
    val status: String,
    val created_at: String,
    val updated_at: String,
    val destinations: List<DestinationHistory>
) : Parcelable