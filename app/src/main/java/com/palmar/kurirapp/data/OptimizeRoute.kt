package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String
) : Parcelable

@Parcelize
data class OptimizeRouteRequest(
    val start: SimpleLocation,
    val vehicle_type: String,
    val destinations: List<SimpleLocation>
) : Parcelable

@Parcelize
data class SimpleLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String = ""
) : Parcelable

@Parcelize
data class OrderedDestination(
    val name: String,
    val latitude: Double,
    val longitude: Double
) : Parcelable

@Parcelize
data class OptimizeRouteResponse(
    val ordered_destinations: List<SimpleLocation>
) : Parcelable