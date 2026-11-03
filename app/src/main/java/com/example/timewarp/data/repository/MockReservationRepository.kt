package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ReservationRepository
import javax.inject.Inject
import javax.inject.Singleton

/*
* In-memory ReservationRepository with one test reservation.
* Used for trying screens without Firestore data.
*/

@Singleton
class MockReservationRepository @Inject constructor() : ReservationRepository {

    // Fake "database" with one booked reservation
    private val reservations = mutableListOf(
        Reservation(reservationId = "test-reservation", reservationState = ReservationState.Reserved)
    )

    override suspend fun getReservations(): MutableList<Reservation>? {
        return reservations
    }

    override suspend fun createReservation(reservation: Reservation) {
        reservations.add(reservation)
    }

    override suspend fun editReservation(campsiteId: String, campsite: Reservation) {
        for (i in reservations.indices) {
            if (reservations[i].reservationId == campsite.reservationId) {
                reservations[i] = campsite
            }
        }
    }

    override suspend fun getReservation(reservationId: String): Reservation? {
        for (reservation in reservations) {
            if (reservation.reservationId == reservationId) {
                return reservation
            }
        }
        return null
    }

    override suspend fun updateReservationState(reservationId: String, state: ReservationState) {
        for (i in reservations.indices) {
            if (reservations[i].reservationId == reservationId) {
                reservations[i] = reservations[i].copy(reservationState = state)
            }
        }
    }
}