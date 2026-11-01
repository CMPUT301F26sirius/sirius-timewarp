package com.example.timewarp.domain.model

import java.time.LocalDate

data class FilterCampsite(
    val location: String = "",
    val guests: Int? = null,
    val checkInt: LocalDate? = null,
    val checkOut: LocalDate? = null,
)

fun Campsite.matches(filters: FilterCampsite): Boolean {
    val locationValid = filters.location.isBlank() ||
            location.address.contains(filters.location.trim(), ignoreCase = true)

    val guestsValid = filters.guests == null || occupancy.maxGuests >= filters.guests

    val datesValid = if (
        filters.checkInt != null && filters.checkOut != null
    ) {
        filters.checkInt.isBefore((filters.checkOut))
    } else {
        true
    }
    return locationValid && guestsValid && datesValid
}