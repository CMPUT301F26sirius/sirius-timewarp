package com.example.timewarp.feature.reservation.cancellation

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Reservation
import com.jsoizo.kotlincsv.csvWriter

/*
* Builds the downloadable cancellation confirmation CSV (US 01.07.02).
* Outstanding: no check-in/check-out columns until Reservation stores dates.
**/

// Column names of the cancellation CSV.
val cancellationCsvHeader = listOf("Reservation #", "Status", "Campsite", "Location", "Booked by")

// Builds a CSV confirming a cancelled reservation
fun cancellationCSV(reservation: Reservation, campsite: Campsite?): String {
    // Empty campsite columns if the campsite couldn't be loaded
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
    return csvWriter().writeAll(listOf(cancellationCsvHeader, row))
}