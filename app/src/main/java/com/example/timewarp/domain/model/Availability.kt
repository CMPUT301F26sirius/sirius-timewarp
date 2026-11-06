package com.example.timewarp.domain.model

/**
 * [Availability] data class
 *
 * This class has yet to be implemented, but will contain availability
 * data for campsites. It is a member of [Campsite] (not a database object).
 * Using availability along with booked reservations yields the available
 * dates.
 */
data class Availability (
    val temp: String = "",

    val startDate: String = "",
    val endDate: String = "",
    val daysOfWeek: List<Int> = (1..7).toList()
)