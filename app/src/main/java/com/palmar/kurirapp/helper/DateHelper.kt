package com.palmar.kurirapp.helper

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateHelper {
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    private val outputFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    fun formatDate(inputDate: String): String {
        return try {
            val date: Date = inputFormat.parse(inputDate) ?: Date()
            outputFormat.format(date)
        } catch (e: Exception) {
            getCurrentDate()
        }
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
}
