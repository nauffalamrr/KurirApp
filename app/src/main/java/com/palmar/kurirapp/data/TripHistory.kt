package com.palmar.kurirapp.data

data class TripHistory(
    val id: Int,
    val date: String,
    val status: String,
    val from: String,
    val destination1: String,
    val destination2: String?,
    val destination3: String?,
    val vehicle: Int
)