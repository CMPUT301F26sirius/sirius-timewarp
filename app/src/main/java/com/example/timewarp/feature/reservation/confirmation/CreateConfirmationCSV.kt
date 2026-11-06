package com.example.timewarp.feature.reservation.confirmation

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Reservation
import com.jsoizo.kotlincsv.csvWriter

/*
* Builds the downloadable reservation confirmation CSV (US 01.05.02).
* Outstanding: no check-in/check-out columns until Reservation stores dates.
*/

// Column names of the confirmation CSV
val confirmationCSVHeader = listOf("Reservation #", "Status", "Campsite", "Location", "Booked by")

// Builds a CSV confirming a reserved booking
fun createConfirmationCSV(reservation: Reservation, campsite: Campsite?): String {
    var campsiteName = ""
    var address = ""
    if (campsite != null) {
        campsiteName = campsite.name
        address = campsite.location.address
    }

    val row = listOf(
        reservation.reservationId,
        reservation.reservationState.name,
        campsiteName,
        address,
        reservation.userId
    )
    return csvWriter().writeAll(listOf(confirmationCSVHeader, row))
}