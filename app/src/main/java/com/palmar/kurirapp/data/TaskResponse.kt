package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class TaskResponse(
    val id: Int,
    var status: String,
    val driver_id: Int,
    val destinations: List<DestinationResponse>
) : Parcelable

@Parcelize
data class MessageResponse(
    val message: String
) : Parcelable