package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.Notification
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
     * Gets waitlisted entries for one lottery.
     *
     * @param lotteryId identifier of the lottery we want to find reservations for
     * @return a list of reservations
     */
    suspend fun getWaitlistedReservations(lotteryId: String): List<Reservation>

    /**
     * Returns the active winner count, initializing old lottery records when needed.
     *
     * @param lotteryId
     * @return active winner count
     */
    suspend fun getOrInitializeActiveWinnerCount(lotteryId: String): Int

    /**
     * Promotes a waitlisted entry if the lottery still has an available winner slot.
     *
     * @param reservationId
     * @param promoteNotification
     * @retrun was promoted
     */
    suspend fun tryPromoteWaitlistedReservation(reservationId: String, promoteNotification: Notification): Boolean

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
     *
     * @return a flow of list of reservations
     */
    fun observeReservations(): Flow<List<Reservation>>
}