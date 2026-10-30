@file:OptIn(ExperimentalUuidApi::class)

package com.example.timewarp.feature.reservation.confirmation

import com.example.timewarp.domain.model.Eric_Reservation
import com.jsoizo.kotlincsv.csvWriter
import kotlin.uuid.ExperimentalUuidApi

val csvHeader = listOf(
    "Reservation #",
    "Booked on",
    "Check-in",
    "Check-out",
    "Booked by",
    "Campsite",
    "Location"
)

fun reservationCSV(reservation: Eric_Reservation): String {
    val row = listOf(
        reservation.reservationId,
        reservation.timestamp.toString(),
        reservation.startDate,
        reservation.endDate,
        reservation.entrant.name,
        reservation.campsite.name,
        reservation.campsite.location.address
    )
    return csvWriter().writeAll(listOf(csvHeader, row))
}