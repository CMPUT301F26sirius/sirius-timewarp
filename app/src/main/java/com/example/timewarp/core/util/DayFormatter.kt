package com.example.timewarp.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/*
* Turns day numbers (days since Jan 1, 1970) into text like "12", "T" or "June 2027".
*/

object DayFormatter {

    private const val MILLISECONDS_PER_DAY = 24L * 60 * 60 * 1000

    // UTC so the date doesn't shift with the phone's time zone
    private val utc = TimeZone.getTimeZone("UTC")

    /** Returns the day of the month, e.g. 12. */
    fun dayOfMonth(day: Int): Int {
        val calendar = Calendar.getInstance(utc)
        calendar.timeInMillis = day.toLong() * MILLISECONDS_PER_DAY
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    /** Returns the first letter of the weekday, e.g. "T". */
    fun weekdayLetter(day: Int): String {
        return format(day, "EEE").take(1)
    }

    /** Returns the month and year, e.g. "June 2027". */
    fun monthYear(day: Int): String {
        return format(day, "MMMM yyyy")
    }

    private fun format(day: Int, pattern: String): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        formatter.timeZone = utc
        return formatter.format(day.toLong() * MILLISECONDS_PER_DAY)
    }
}