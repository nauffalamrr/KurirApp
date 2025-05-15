package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoutePoint(
    val latitude: Double,
    val longitude: Double,
    val name: String = ""
) : Parcelable