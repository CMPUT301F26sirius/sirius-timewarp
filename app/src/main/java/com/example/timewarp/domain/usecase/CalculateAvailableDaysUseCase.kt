package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Period
import jakarta.inject.Inject

/*
* Finds which days of a lottery's booking period are still free (US 01.04.01).
* Outstanding: ignores campsite Availability (model is a placeholder).
* Calculates bookable days in a booking period.
* Days are day numbers (days since Jan 1, 1970).
* A stay from June 12 to 14 takes the nights of June 12 and 13.
*/

class CalculateAvailableDaysUseCase @Inject constructor() {

    /**
     * Returns days in [bookingPeriod] not taken by [bookedStays].
     *
     *  @param bookingPeriod booking period, both ends included.
     *  @param bookedStays active stays; check-out day is not taken.
     *  @return free days in order, or empty if the period is invalid.
     */

    operator fun invoke(bookingPeriod: Period, bookedStays: List<Period>): List<Int> {
        val availableDays = mutableListOf<Int>()

        // Invalid period: nothing is bookable
        if (bookingPeriod.end < bookingPeriod.start) {
            return availableDays
        }

        // Keep each day that no stay occupies
        for (day in bookingPeriod.start..bookingPeriod.end) {
            if (!isDayBooked(day, bookedStays)) {
                availableDays.add(day)
            }
        }
        return availableDays
    }

    // True if any stay occupies the night of this day
    private fun isDayBooked(day: Int, bookedStays: List<Period>): Boolean {
        for (stay in bookedStays) {
            // Check-out day is free, so stop before stay.end
            if (day >= stay.start && day < stay.end) {
                return true
            }
        }
        return false
    }
}