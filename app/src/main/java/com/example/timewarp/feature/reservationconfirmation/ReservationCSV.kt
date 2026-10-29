package com.example.timewarp.feature.reservationconfirmation
import com.jsoizo.kotlincsv.csvWriter

val csvHeader = listOf(
    "Reservation #",
    "Booked on",
    "Check-in",
    "Check-out",
    "Booked by",
    "Campsite",
    "Location"
)

fun reservationCSV(confirmation: ReservationConfirmation): String {
    val row = listOf(
        confirmation.reservationId,
        confirmation.reservationTimestamp.toString(),
        confirmation.reservationStartDate,
        confirmation.reservationEndDate,
        confirmation.entrantName,
        confirmation.campsiteName,
        confirmation.campsiteLocation
    )
    return csvWriter().writeAll(listOf(csvHeader, row))
}
