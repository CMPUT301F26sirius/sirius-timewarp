package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.usecase.CalculateAvailableDaysUseCase
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test

class CalculateAvailableDaysUseCaseTest {

    // November 10, 2027 as a day number
    private val nov10 = 21132
    private val nov11 = nov10 + 1
    private val nov12 = nov10 + 2
    private val nov13 = nov10 + 3
    private val nov14 = nov10 + 4
    private val nov15 = nov10 + 5
    private val nov16 = nov10 + 6

    private val bookingPeriod = Period(start = nov10, end = nov16)

    private lateinit var calculateAvailableDays: CalculateAvailableDaysUseCase

    @Before
    fun setUp() {
        calculateAvailableDays = CalculateAvailableDaysUseCase()
    }

    @Test
    fun noReservations_allDaysAvailable() {
        val result = calculateAvailableDays(bookingPeriod, emptyList())

        val expected = listOf(nov10, nov11, nov12, nov13, nov14, nov15, nov16)
        TestCase.assertEquals(expected, result)
    }

    @Test
    fun oneStay_stayNightsRemoved_checkOutDayStillAvailable() {
        // Takes nights of November 12, 13
        val stay = Period(start = nov12, end = nov14)

        val result = calculateAvailableDays(bookingPeriod, listOf(stay))

        val expected = listOf(nov10, nov11, nov14, nov15, nov16)
        TestCase.assertEquals(expected, result)
    }

    @Test
    fun twoStays_bothRemoved() {
        val firstStay = Period(start = nov10, end = nov12)
        val secondStay = Period(start = nov15, end = nov16)

        val result = calculateAvailableDays(bookingPeriod, listOf(firstStay, secondStay))

        val expected = listOf(nov12, nov13, nov14, nov16)
        TestCase.assertEquals(expected, result)
    }

    @Test
    fun stayStartsBeforeBookingPeriod_onlyOverlapRemoved() {
        // November 8 to 11: only November 10 overlaps
        val stay = Period(start = nov10 - 2, end = nov11)

        val result = calculateAvailableDays(bookingPeriod, listOf(stay))

        val expected = listOf(nov11, nov12, nov13, nov14, nov15, nov16)
        TestCase.assertEquals(expected, result)
    }

    @Test
    fun invalidBookingPeriod_noDaysAvailable() {
        val invalidPeriod = Period(start = nov16, end = nov10)

        val result = calculateAvailableDays(invalidPeriod, emptyList())

        TestCase.assertEquals(emptyList<Int>(), result)
    }
}