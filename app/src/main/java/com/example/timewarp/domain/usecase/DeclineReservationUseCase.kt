package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ReservationRepository
import jakarta.inject.Inject

/*
 * Lets a lottery winner decline their won reservation (US 01.04.03).
 * Outstanding: the new winner is rolled by the auto-draw, which hooks into the state change.
 * Declines a won reservation by changing its state from Pending to Declined.
 * The reservation is kept, not deleted, so the free spot can be redrawn.
 */
class DeclineReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {

    /** Returns true if [reservation] can be declined. Only Pending (won) ones can. */
    fun canDecline(reservation: Reservation): Boolean {
        if (reservation.reservationState == ReservationState.Pending) {
            return true
        }
        return false
    }

    /**
     * Declines the reservation with [reservationId].
     *
     * @return true if it was declined, false if it doesn't exist or isn't Pending.
     */
    suspend operator fun invoke(reservationId: String): Boolean {
        val reservation = reservationRepository.getReservation(reservationId)

        if (reservation == null) {
            return false
        }

        if (!canDecline(reservation)) {
            return false
        }

        reservationRepository.updateReservationState(reservationId, ReservationState.Declined)
        return true
    }
}