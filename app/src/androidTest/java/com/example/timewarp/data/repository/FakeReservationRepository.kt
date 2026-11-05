package com.example.timewarp.data.repository

import android.util.Log
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ReservationRepository
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
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

    private val nowSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt()
    private val oneDay = TimeUnit.DAYS.toSeconds(1).toInt()

    /** The fake "database": reservationId → Reservation. */
    private var reservations: Map<String, Reservation> =
        ReservationState.entries.flatMapIndexed { index, state ->
            listOf(0, 1).map { copy ->
                val number = index * 2 + copy
                Reservation(
                    reservationId = "fake-$number",
                    reservationState = state,
                    timestamp = nowSeconds - number * 3 * oneDay, // 0, 3, 6, 9... days ago
                    campsiteId = "fake-campsite-$number",
                    userId = "test-user",
                )
            }
        }.associateBy { it.reservationId }

    override suspend fun getReservations(): MutableList<Reservation>? {
        simulateNetwork()
        return reservations.values.toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createReservation(reservation: Reservation) {
        simulateNetwork()
        reservations = reservations + (reservation.reservationId to reservation)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun editReservation(reservationId: String, campsite: Reservation) {
        simulateNetwork()
        reservations = reservations + (reservationId to campsite)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun updateReservationState(reservationId: String, state: ReservationState) {
        simulateNetwork()
        val existing = reservations[reservationId] ?: return
        reservations = reservations + (reservationId to existing.copy(reservationState = state))
        Log.d("FakeReservationRepo", "Reservation $reservationId is now $state")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getReservation(reservationId: String): Reservation? {
        simulateNetwork()
        return reservations[reservationId]
    }

    fun setReservations(list: List<Reservation>){
        reservations = list.associateBy {it.reservationId}
    }

    private suspend fun simulateNetwork() = delay(500)
}
