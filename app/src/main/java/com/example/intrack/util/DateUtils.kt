package com.example.intrack.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd", Locale.US)
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

    fun formatWeeklyTimeTier(timeMillis: Long): String {
        val nowCal = java.util.Calendar.getInstance()
        val txCal = java.util.Calendar.getInstance().apply { timeInMillis = timeMillis }

        val nowYear = nowCal.get(java.util.Calendar.YEAR)
        val txYear = txCal.get(java.util.Calendar.YEAR)
        val nowWeek = nowCal.get(java.util.Calendar.WEEK_OF_YEAR)
        val txWeek = txCal.get(java.util.Calendar.WEEK_OF_YEAR)

        val weekDiff = if (nowYear == txYear) {
            nowWeek - txWeek
        } else {
            val daysDiff = ((nowCal.timeInMillis - txCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            daysDiff / 7
        }

        return when {
            weekDiff <= 0 -> "This Week"
            weekDiff == 1 -> "Last Week"
            weekDiff == 2 -> "2 Weeks Ago"
            weekDiff == 3 -> "3 Weeks Ago"
            else -> "Earlier"
        }
    }

    fun formatRelativeDateGroup(timeMillis: Long, monthName: String = "Month"): String {
        val nowCal = java.util.Calendar.getInstance()
        val txCal = java.util.Calendar.getInstance().apply { timeInMillis = timeMillis }

        val nowYear = nowCal.get(java.util.Calendar.YEAR)
        val txYear = txCal.get(java.util.Calendar.YEAR)
        val nowWeek = nowCal.get(java.util.Calendar.WEEK_OF_YEAR)
        val txWeek = txCal.get(java.util.Calendar.WEEK_OF_YEAR)

        val weekDiff = if (nowYear == txYear) {
            nowWeek - txWeek
        } else {
            val daysDiff = ((nowCal.timeInMillis - txCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            daysDiff / 7
        }

        return when {
            weekDiff <= 0 -> "This Week"
            weekDiff == 1 -> "Last Week"
            weekDiff == 2 -> "2 Weeks Ago"
            weekDiff == 3 -> "3 Weeks Ago"
            else -> "Earlier in $monthName"
        }
    }
}
