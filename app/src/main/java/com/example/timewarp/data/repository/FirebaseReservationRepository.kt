package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Availability
import com.example.timewarp.domain.model.Pricing
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.ReservationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.example.timewarp.domain.model.ReservationState
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [ReservationRepository].
 *
 * Stores and retrieves reservations from the Firestore `reservation` collection.
 *
 * @property firestore Firestore instance used to access reservations.
 */
@Singleton
class FirebaseReservationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReservationRepository {
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getReservation(reservationId: String): Reservation? {
        val snapshot = firestore.collection("reservation")
            .document(reservationId)
            .get()
            .await()

        // No document with this id
        if (!snapshot.exists()) {
            return null
        }

        val reservation = snapshot.toObject(Reservation::class.java)
        if (reservation == null) {
            return null
        }

        // The id is the document name, not a field inside it
        return reservation.copy(reservationId = reservationId)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun updateReservationState(reservationId: String, state: ReservationState) {
        firestore.collection("reservation")
            .document(reservationId)
            .update("reservationState", state.name)
            .await()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getReservations(): MutableList<Reservation>? {
        val snapshot = firestore.collection("reservation")
            .get()
            .await()

        val reservations = mutableListOf<Reservation>()
        for (document in snapshot.documents) {
            val reservation = document.toObject(Reservation::class.java) ?: continue


            reservations.add(reservation.copy(reservationId = document.id))
        }
        return reservations
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createReservation(reservation: Reservation) {
        firestore.collection("reservation")
            .document(reservation.reservationId)
            .set(reservation)
            .await()
    }


    /**
     * Replaces the stored reservation with [reservationId] with new data.
     *
     * @param reservationId the reservation to overwrite.
     * @param campsite the new reservation data (named campsite in the interface).
     */
    override suspend fun editReservation(reservationId: String, campsite: Reservation) {
        firestore.collection("reservation")
            .document(reservationId)
            .set(campsite.copy(reservationId = reservationId))
            .await()
    }
}
