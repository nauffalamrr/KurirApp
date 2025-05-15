package com.palmar.kurirapp.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Result(
    val number: Int,
    val destination: String
) : Parcelable