package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Availability
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [FindBookableDaysUseCase] (US 01.04.01).
 */
class FindBookableDaysUseCaseTest {

    private val findBookableDays = FindBookableDaysUseCase(CalculateAvailableDaysUseCase())

    // Wednesday, December 22, 2027
    private val dec22 = 21174

    // Stay window over Christmas week, Wednesday Dec 22 to Tuesday Dec 28
    private val stayWindow = Period(start = dec22, end = dec22 + 6)
    private val allWeek = (dec22..dec22 + 6).toList()

    // Owner set no date limits and opened every weekday
    private val openAvailability = Availability()

    private val myReservationId = "mine"

    // Builds a reservation at the campsite with the given state and saved dates
    private fun reservation(id: String, state: ReservationState, start: String, end: String): Reservation {
        return Reservation(
            reservationId = id,
            reservationState = state,
            datesBooked = Availability(startDate = start, endDate = end)
        )
    }

    @Test
    fun noBookings_allDaysBookable() {
        val days = findBookableDays(stayWindow, openAvailability, emptyList(), myReservationId)

        assertEquals(allWeek, days)
    }

    @Test
    fun reservedStay_blocksNights_butNotCheckOutDay() {
        // Christmas Eve to Boxing Day takes the nights of Dec 24 and Dec 25
        val booked = reservation("other", ReservationState.Reserved, "2027-12-24", "2027-12-26")

        val days = findBookableDays(stayWindow, openAvailability, listOf(booked), myReservationId)

        assertEquals(listOf(dec22, dec22 + 1, dec22 + 4, dec22 + 5, dec22 + 6), days)
    }

    @Test
    fun pendingAndCancelled_doNotBlock() {
        val pending = reservation("a", ReservationState.Pending, "2027-12-24", "2027-12-26")
        val cancelled = reservation("b", ReservationState.Canceled, "2027-12-24", "2027-12-26")

        val days = findBookableDays(stayWindow, openAvailability, listOf(pending, cancelled), myReservationId)

        assertEquals(allWeek, days)
    }

    @Test
    fun ownReservation_doesNotBlockItself() {
        val mine = reservation(myReservationId, ReservationState.Reserved, "2027-12-24", "2027-12-26")

        val days = findBookableDays(stayWindow, openAvailability, listOf(mine), myReservationId)

        assertEquals(allWeek, days)
    }

    @Test
    fun reservedWithoutDates_doesNotBlock() {
        val noDates = reservation("other", ReservationState.Reserved, "", "")

        val days = findBookableDays(stayWindow, openAvailability, listOf(noDates), myReservationId)

        assertEquals(allWeek, days)
    }

    @Test
    fun ownerDateWindow_limitsDays() {
        // Owner only opens from Christmas Eve to Boxing Day
        val ownerWindow = Availability(startDate = "2027-12-24", endDate = "2027-12-26")

        val days = findBookableDays(stayWindow, ownerWindow, emptyList(), myReservationId)

        assertEquals(listOf(dec22 + 2, dec22 + 3, dec22 + 4), days)
    }

    @Test
    fun ownerWeekdays_limitDays() {
        // Only Sunday (1) and Saturday (7), so Christmas (Sat Dec 25) and Boxing Day (Sun Dec 26)
        val weekendsOnly = Availability(daysOfWeek = listOf(1, 7))

        val days = findBookableDays(stayWindow, weekendsOnly, emptyList(), myReservationId)

        assertEquals(listOf(dec22 + 3, dec22 + 4), days)
    }

    @Test
    fun rangeFree_whenEveryNightIsBookable() {
        val bookable = listOf(dec22, dec22 + 1, dec22 + 2)

        // Check in Dec 22, check out Christmas Day, so nights 22, 23 and 24
        assertTrue(FindBookableDaysUseCase.isRangeFree(dec22, dec22 + 3, bookable))
    }

    @Test
    fun rangeNotFree_whenANightIsBooked() {
        val bookable = listOf(dec22, dec22 + 2)

        assertFalse(FindBookableDaysUseCase.isRangeFree(dec22, dec22 + 3, bookable))
    }

    @Test
    fun rangeNotFree_withZeroNights() {
        val bookable = listOf(dec22)

        assertFalse(FindBookableDaysUseCase.isRangeFree(dec22, dec22, bookable))
    }

    @Test
    fun checkOutDay_isSelectable_evenIfThatNightIsNot() {
        val bookable = listOf(dec22)

        assertTrue(FindBookableDaysUseCase.isSelectableDay(dec22 + 1, bookable))
        assertFalse(FindBookableDaysUseCase.isSelectableDay(dec22 + 2, bookable))
    }
}