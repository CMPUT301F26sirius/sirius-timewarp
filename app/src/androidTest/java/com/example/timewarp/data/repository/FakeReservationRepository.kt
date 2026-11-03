package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ReservationRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [ReservationRepository].
 *
 * Stores and retrieves reservations from a `reservation` collection.
 */
@Singleton
class FakeReservationRepository @Inject constructor() : ReservationRepository {
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getReservations(): MutableList<Reservation>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createReservation(reservation: Reservation) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun editReservation(campsiteId: String, campsite: Reservation) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getReservation(reservationId: String): Reservation? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun updateReservationState(reservationId: String, state: ReservationState) {
        TODO("Not yet implemented")
    }
}
