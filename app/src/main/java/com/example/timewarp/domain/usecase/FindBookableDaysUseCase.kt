package com.example.timewarp.domain.usecase

import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Availability
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import jakarta.inject.Inject

/*
 * Works out which days a lottery winner can book at one campsite (US 01.04.01).
 * A day is bookable if it's in the stay window, inside the owner's dates and weekdays,
 * and no Reserved booking at the campsite takes that night.
 * Outstanding: dates are stored as yyyy-MM-dd strings in Availability, which may be refactored.
 */

/**
 * Finds the bookable days for one campsite and checks picked date ranges.
 * Days are day numbers (days since Jan 1, 1970).
 */
class FindBookableDaysUseCase @Inject constructor(
    private val calculateAvailableDays: CalculateAvailableDaysUseCase
) {

    /**
     * Returns the days in [stayWindow] that can still be booked.
     *
     * @param stayWindow the range of days the winner may stay in, both ends included.
     * @param ownerAvailability the campsite's dates and weekdays set by the owner.
     * @param campsiteReservations reservations at the same campsite; only Reserved ones block days.
     * @param reservationId the reservation being booked, so it never blocks itself.
     * @return bookable days in order.
     */
    operator fun invoke(
        stayWindow: Period,
        ownerAvailability: Availability,
        campsiteReservations: List<Reservation>,
        reservationId: String
    ): List<Int> {
        // Remove nights other people already booked
        val bookedStays = findBookedStays(campsiteReservations, reservationId)
        val freeDays = calculateAvailableDays(stayWindow, bookedStays)

        // Owner's open and close dates; empty or invalid text means no limit
        val ownerStart = DayFormatter.fromDateString(ownerAvailability.startDate)
        val ownerEnd = DayFormatter.fromDateString(ownerAvailability.endDate)

        val bookableDays = mutableListOf<Int>()
        for (day in freeDays) {
            // Skip days before the owner opens the campsite
            if (ownerStart != null && day < ownerStart) {
                continue
            }
            // Skip days after the owner closes the campsite
            if (ownerEnd != null && day > ownerEnd) {
                continue
            }
            // Skip weekdays the owner didn't open
            if (!ownerAvailability.daysOfWeek.contains(DayFormatter.weekday(day))) {
                continue
            }
            bookableDays.add(day)
        }
        return bookableDays
    }

    // Turns other Reserved bookings with saved dates into stays that block nights
    private fun findBookedStays(reservations: List<Reservation>, reservationId: String): List<Period> {
        val stays = mutableListOf<Period>()
        for (reservation in reservations) {
            // The reservation being booked never blocks itself
            if (reservation.reservationId == reservationId) {
                continue
            }
            // Only confirmed bookings take nights
            if (reservation.reservationState != ReservationState.Reserved) {
                continue
            }

            val start = DayFormatter.fromDateString(reservation.datesBooked.startDate)
            val end = DayFormatter.fromDateString(reservation.datesBooked.endDate)

            // Bookings without saved dates don't block anything
            if (start == null || end == null) {
                continue
            }
            stays.add(Period(start = start, end = end))
        }
        return stays
    }

    companion object {
        /**
         * True if [day] can be tapped in the date picker: it's bookable itself (check-in),
         * or the day before it is (check-out).
         */
        fun isSelectableDay(day: Int, bookableDays: List<Int>): Boolean {
            // Bookable night, so it works as a check-in
            if (bookableDays.contains(day)) {
                return true
            }
            // Night before is bookable, so it works as a check-out
            if (bookableDays.contains(day - 1)) {
                return true
            }
            return false
        }

        /**
         * True if every night from [checkIn] up to (not including) [checkOut] is bookable.
         * The check-out day isn't a night, so it doesn't need to be free.
         */
        fun isRangeFree(checkIn: Int, checkOut: Int, bookableDays: List<Int>): Boolean {
            // Need at least one night
            if (checkOut <= checkIn) {
                return false
            }
            // Every night of the stay must be bookable
            for (night in checkIn until checkOut) {
                if (!bookableDays.contains(night)) {
                    return false
                }
            }
            return true
        }
    }
}