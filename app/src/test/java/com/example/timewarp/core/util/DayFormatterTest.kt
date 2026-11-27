package com.example.timewarp.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [DayFormatter]'s date conversions and weekday numbering (US 01.04.01).
 */
class DayFormatterTest {

    // Wednesday, November 10, 2027
    private val nov10 = 21132

    @Test
    fun toDateString_formatsDayNumber() {
        assertEquals("2027-11-10", DayFormatter.toDateString(nov10))
    }

    @Test
    fun fromDateString_parsesStoredDate() {
        assertEquals(nov10, DayFormatter.fromDateString("2027-11-10"))
    }

    @Test
    fun storedDate_worksBothWays() {
        val text = DayFormatter.toDateString(20981)

        assertEquals("2027-06-12", text)
        assertEquals(20981, DayFormatter.fromDateString(text))
    }

    @Test
    fun firstDay_isZero() {
        assertEquals(0, DayFormatter.fromDateString("1970-01-01"))
    }

    @Test
    fun emptyText_returnsNull() {
        assertNull(DayFormatter.fromDateString(""))
    }

    @Test
    fun wrongFormat_returnsNull() {
        assertNull(DayFormatter.fromDateString("10/11/2027"))
        assertNull(DayFormatter.fromDateString("hello"))
    }

    @Test
    fun impossibleDate_returnsNull() {
        assertNull(DayFormatter.fromDateString("2027-13-01"))
    }

    @Test
    fun weekday_usesSundayAsOne() {
        // November 10, 2027 is a Wednesday: Sun=1, Mon=2, Tue=3, Wed=4
        assertEquals(4, DayFormatter.weekday(nov10))
    }

    @Test
    fun millis_worksBothWays() {
        val millis = DayFormatter.toMillis(nov10)

        assertEquals(nov10, DayFormatter.fromMillis(millis))
        // Any time later that day is still the same day
        assertEquals(nov10, DayFormatter.fromMillis(millis + 5 * 60 * 60 * 1000))
    }
}