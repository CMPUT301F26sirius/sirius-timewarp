package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.ReservationRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [ReservationRepository].
 *
 * Stores and retrieves reservations from Firestore.
 *
 * @property firestore Firestore instance used to access reservations.
 */
@Singleton
class FirebaseReservationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReservationRepository {
    override suspend fun getReservations(): MutableList<Reservation>? {
        TODO("Not yet implemented")
    }

    override suspend fun createReservation(reservation: Reservation) {
        TODO("Not yet implemented")
    }

    override suspend fun editReservation(campsiteId: String, campsite: Reservation) {
        TODO("Not yet implemented")
    }

    override suspend fun getReservation(reservationId: String): Reservation? {
        TODO("Not yet implemented")
    }
}
