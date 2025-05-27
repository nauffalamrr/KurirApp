package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Destination(
    val id: Int,
    val location: Location,
    val sequence_order: Int,
    val latitude: Double,
    val longitude: Double
) : Parcelable

@Parcelize
data class DestinationResponse(
    val id: Int,
    val destination_name: String,
    val sequence_order: Int,
    val latitude: Double?,
    val longitude: Double?
) : Parcelable

@Parcelize
data class Task(
    val id: Int,
    var status: String,
    val driver_id: Int,
    val destinations: List<Destination>
) : Parcelable