package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
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
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getWaitlistedReservations(lotteryId: String): List<Reservation> =
        reservationsState.value.values.filter {
            it.lotteryId == lotteryId && it.reservationState == ReservationState.Waitlisted
        }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getOrInitializeActiveWinnerCount(lotteryId: String): Int =
        reservationsState.value.values.count {
            it.lotteryId == lotteryId &&
                (it.reservationState == ReservationState.Pending ||
                    it.reservationState == ReservationState.Reserved)
        }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun tryPromoteWaitlistedReservation(reservationId: String, promoteNotification: Notification): Boolean {
        while (true) {
            val current = reservationsState.value
            val reservation = current[reservationId] ?: return false
            if (reservation.lotteryId.isBlank() ||
                reservation.reservationState != ReservationState.Waitlisted ||
                promoteNotification.notificationId.isBlank()
            ) {
                return false
            }

            val promoted = current +
                (reservationId to reservation.copy(reservationState = ReservationState.Pending))
            if (reservationsState.compareAndSet(current, promoted)) return true
        }
    }

    /**
     * Changes the state of a stored reservation.
     * @param reservationId ID of the reservation.
     * @param state the new state.
     **/
    override suspend fun updateReservationState(reservationId: String, state: ReservationState) {
        while (true) {
            val current = reservationsState.value
            val reservation = current[reservationId] ?: return
            val updated = current + (reservationId to reservation.copy(reservationState = state))
            if (reservationsState.compareAndSet(current, updated)) return
        }
    }

    /**
     * Removes the reservation with [reservationId] from the fake collection.
     *
     * @param reservationId ID of the reservation to remove.
     **/
    override suspend fun deleteReservation(reservationId: String) {
        reservationsState.value = reservationsState.value - reservationId
    }


    /**
     * Sets the reservation with [reservationId] to Canceled.
     *
     * @param reservationId ID of the reservation to cancel.
     **/
    override suspend fun cancelReservation(reservationId: String) {
        updateReservationState(reservationId, ReservationState.Canceled)
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

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeReservations(): Flow<List<Reservation>> =
        reservationsState.map { it.values.toList() }
}