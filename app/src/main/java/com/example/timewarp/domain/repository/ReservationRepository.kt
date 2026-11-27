package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository for storing and retrieving [Reservation] objects.
 */
interface ReservationRepository {
    /**
     * @return all reservations, or `null` when no collection result is available.
     */
    suspend fun getReservations(): MutableList<Reservation>?

    /**
     * @param reservation reservation to create.
     */
    suspend fun createReservation(reservation: Reservation)

    /**
     * @param reservationId identifier of the reservation to edit.
     * @param campsite replacement reservation data.
     */
    suspend fun editReservation(reservationId: String, campsite: Reservation)

    /**
     * @param reservationId identifier of the reservation to retrieve.
     * @return the matching reservation, or `null` when it does not exist.
     */
    suspend fun getReservation(reservationId: String): Reservation?

    /**
     * @param reservationId identifier of the reservation to update.
     * @param state new state for the reservation.
     */
    suspend fun updateReservationState(reservationId: String, state: ReservationState)

    /**
     * deletes a reservation
     *
     * @param reservationId Identifier of the reservation to retrieve
     */
    suspend fun deleteReservation(reservationId: String)

    /**
     * Cancels a reservation by setting its state to Canceled. The reservation stays in
     * the database.
     *
     * @param reservationId identifier of the reservation to cancel.
     */
    suspend fun cancelReservation(reservationId: String)
    
    /**
     * Responsible for live updates
     */
    fun observeReservations(): Flow<List<Reservation>> = flow {
        emit(getReservations().orEmpty())
    }
}