package com.example.intrack.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        return sdf.format(Date(timeMillis))
    }

    fun formatShortDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("MMM d", Locale.US)
        return sdf.format(Date(timeMillis))
    }

    fun formatDateTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.US)
        return sdf.format(Date(timeMillis))
    }

    fun formatMonth(timeMillis: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        return sdf.format(Date(timeMillis))
    }

    fun getCurrentMonthName(): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        return sdf.format(Date())
    }

    fun isCurrentMonth(timeMillis: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMM", Locale.US)
        return sdf.format(Date(timeMillis)) == sdf.format(Date())
    }

    fun getRecentMonthsList(count: Int = 6): List<String> {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        val cal = java.util.Calendar.getInstance()
        val list = mutableListOf<String>()
        for (i in 0 until count) {
            list.add(sdf.format(cal.time))
            cal.add(java.util.Calendar.MONTH, -1)
        }
        return list
    }

    fun matchesMonth(timeMillis: Long, monthYearStr: String): Boolean {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        return sdf.format(Date(timeMillis)).equals(monthYearStr, ignoreCase = true)
    }

    fun getMillisForMonthOffset(monthsAgo: Int, dayOfMonth: Int = 15): Long {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, -monthsAgo)
        cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
        return cal.timeInMillis
    }

    fun daysBetween(startMillis: Long, endMillis: Long): Long {
        val diff = endMillis - startMillis
        return if (diff > 0) diff / (1000 * 60 * 60 * 24) else 0
    }
}
