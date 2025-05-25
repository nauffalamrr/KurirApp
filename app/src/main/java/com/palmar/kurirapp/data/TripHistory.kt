package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TripHistory(
    val date: String,
    val status: String,
    val from: String,
    val destination1: String,
    val destination2: String,
    val destination3: String,
    val vehicle: String
) : Parcelable