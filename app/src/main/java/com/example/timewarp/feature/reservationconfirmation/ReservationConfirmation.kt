package com.example.timewarp.feature.reservationconfirmation
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import java.time.LocalDate
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi


class ReservationConfirmation (
    reservation: Reservation
){
    val reservationId: String = reservation.id
    val reservationTimestamp: Instant = reservation.timestamp
    val reservationStartDate: String = reservation.startDate
    val reservationEndDate: String = reservation.endDate
    val entrantName: String = reservation.entrant.name
    @OptIn(ExperimentalUuidApi::class)
    val campsiteName: String = reservation.campsite.name
    @OptIn(ExperimentalUuidApi::class)
    val campsiteLocation: String = reservation.campsite.location.address

}