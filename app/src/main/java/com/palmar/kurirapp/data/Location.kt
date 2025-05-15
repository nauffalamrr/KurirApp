package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val name: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) : Parcelable

@Parcelize
data class OptimizeRouteRequest(
    val vehicleType: String,
    val startLocation: Location,
    val destinations: List<Location>
) : Parcelable

data class OptimizeRouteResponse(
    val orderedDestinations: List<Location>
)
