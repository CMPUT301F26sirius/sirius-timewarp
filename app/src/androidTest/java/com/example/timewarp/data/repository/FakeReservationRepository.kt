package com.example.timewarp.data.repository

import android.util.Log
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ReservationRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [ReservationRepository].
 *
 * Stores reservations in memory instead of Firestore.
 **/
@Singleton
class FakeReservationRepository @Inject constructor() : ReservationRepository {
    private val reservationsState = MutableStateFlow<Map<String, Reservation>>(emptyMap())

    /**
     * Returns every stored reservation.
     *
     * @return all reservations in the fake collection.
     **/
    override suspend fun getReservations(): MutableList<Reservation>? {
        return reservationsState.value.values.toMutableList()
    }

    /**
     * Stores a reservation, replacing any with the same ID.
     *
     * @param reservation reservation to store.
     **/
    override suspend fun createReservation(reservation: Reservation) {
        addReservation(reservation)
    }

    /**
     * Replaces the reservation with [reservationId].
     *
     * @param reservationId ID of the reservation to replace.
     * @param campsite the new reservation data.
     **/
    override suspend fun editReservation(reservationId: String, campsite: Reservation) {
        val updated = campsite.copy(reservationId = reservationId)
        reservationsState.value = reservationsState.value + (reservationId to updated)
    }

    /**
     * Retrieves a reservation by ID.
     *
     * @param reservationId ID of the reservation.
     * @return the reservation, or `null` when it does not exist.
     **/
    override suspend fun getReservation(reservationId: String): Reservation? {
        return reservationsState.value[reservationId]
    }

    /**
     * Changes the state of a stored reservation.
     * @param reservationId ID of the reservation.
     * @param state the new state.
     **/
    override suspend fun updateReservationState(reservationId: String, state: ReservationState) {
        val reservation = reservationsState.value[reservationId]

        // Nothing to update
        if (reservation == null) {
            return
        }

        val updated = reservation.copy(reservationState = state)
        reservationsState.value = reservationsState.value + (reservationId to updated)
    }

    /**
     * Removes all reservations from the fake collection.
     **/
    fun clear() {
        reservationsState.value = emptyMap()
    }

    /**
     * Adds a reservation synchronously for test setup.
     *
     * @param reservation reservation to add.
     **/
    fun addReservation(reservation: Reservation) {
        reservationsState.value = reservationsState.value + (reservation.reservationId to reservation)
    }
}
