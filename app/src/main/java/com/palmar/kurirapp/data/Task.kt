package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Destination(
    val id: Int,
    val location: Location,
    val sequence_order: Int
) : Parcelable

@Parcelize
data class Task(
    val id: Int,
    var status: String,
    val driver_id: Int,
    val destinations: List<Destination>
) : Parcelable