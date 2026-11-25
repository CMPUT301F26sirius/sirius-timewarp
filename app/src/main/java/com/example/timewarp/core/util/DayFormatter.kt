package com.example.timewarp.core.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/*
* Turns day numbers (days since Jan 1, 1970) into text like "12", "T" or "June 2027",
* and converts between day numbers, date-picker milliseconds and stored dates like "2027-06-12".
*/

object DayFormatter {

    private const val MILLISECONDS_PER_DAY = 24L * 60 * 60 * 1000

    // Format of dates stored in Availability (campsite availability and datesBooked)
    private const val STORED_DATE_PATTERN = "yyyy-MM-dd"

    // UTC so the date doesn't shift with the phone's time zone
    private val utc = TimeZone.getTimeZone("UTC")

    /** Returns the day of the month, e.g. 12. */
    fun dayOfMonth(day: Int): Int {
        val calendar = Calendar.getInstance(utc)
        calendar.timeInMillis = toMillis(day)
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Returns the weekday as a number from 1 (Sunday) to 7 (Saturday).
     * Same numbering as the owner's available days on Create Campsite.
     */
    fun weekday(day: Int): Int {
        val calendar = Calendar.getInstance(utc)
        calendar.timeInMillis = toMillis(day)
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    /** Returns the first letter of the weekday, e.g. "T". */
    fun weekdayLetter(day: Int): String {
        return format(day, "EEE").take(1)
    }

    /** Returns the month and year, e.g. "June 2027". */
    fun monthYear(day: Int): String {
        return format(day, "MMMM yyyy")
    }

    /** Returns a short date for the screen, e.g. "Nov 10, 2027". */
    fun shortDate(day: Int): String {
        return format(day, "MMM d, yyyy")
    }

    /** Returns the start of [day] in UTC milliseconds, the unit date pickers use. */
    fun toMillis(day: Int): Long {
        return day.toLong() * MILLISECONDS_PER_DAY
    }

    /** Returns the day number that milliseconds (UTC) falls on, e.g. a date picker's selection. */
    fun fromMillis(millis: Long): Int {
        return Math.floorDiv(millis, MILLISECONDS_PER_DAY).toInt()
    }

    /** Turns a day number into a stored date, e.g. 21132 becomes "2027-11-10". */
    fun toDateString(day: Int): String {
        // Locale.US so the digits are always 0-9, whatever the phone language
        val formatter = SimpleDateFormat(STORED_DATE_PATTERN, Locale.US)
        formatter.timeZone = utc
        return formatter.format(toMillis(day))
    }

    /**
     * Turns a stored date like "2027-11-10" into a day number.
     *
     * @return the day number, or null if [text] isn't a valid yyyy-MM-dd date.
     */
    fun fromDateString(text: String): Int? {
        val parser = SimpleDateFormat(STORED_DATE_PATTERN, Locale.US)
        parser.timeZone = utc
        // Reject impossible dates like month 13 instead of rolling them over
        parser.isLenient = false

        try {
            val date = parser.parse(text)
            if (date == null) {
                return null
            }
            return fromMillis(date.time)
        } catch (error: ParseException) {
            return null
        }
    }

    private fun format(day: Int, pattern: String): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        formatter.timeZone = utc
        return formatter.format(toMillis(day))
    }
}