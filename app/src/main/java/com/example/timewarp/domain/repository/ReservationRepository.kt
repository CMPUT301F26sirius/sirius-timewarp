package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Reservation

interface ReservationRepository {
    suspend fun getReservations(): MutableList<Reservation>?

    suspend fun createReservation(reservation: Reservation)

    suspend fun editReservation(campsiteId: String, campsite: Reservation)

    suspend fun getReservation(reservationId: String): Reservation?
}