package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState

/**
 * [ReservationRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface ReservationRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getReservations(): MutableList<Reservation>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun createReservation(reservation: Reservation)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun editReservation(reservationId: String, campsite: Reservation)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getReservation(reservationId: String): Reservation?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun updateReservationState(reservationId: String, state: ReservationState)
}