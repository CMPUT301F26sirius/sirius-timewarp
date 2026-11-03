package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ReservationRepository
import jakarta.inject.Inject

/*
* Cancels an entrant's reservation (US 01.07.01).
* Outstanding: doesn't block cancelling a stay that already happened (dates not stored yet).
*/

class CancelReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {
    fun canCancel(reservation: Reservation): Boolean {
        if (reservation.reservationState == ReservationState.Reserved) {
            return true
        }
        return false
    }

    suspend operator fun invoke(reservationId: String): Boolean {
        val reservation = reservationRepository.getReservation(reservationId)

        // Nothing to cancel
        if (reservation == null) {
            return false
        }

        // Already cancelled, rejected, declined, etc.
        if (!canCancel(reservation)) {
            return false
        }

        reservationRepository.updateReservationState(reservationId, ReservationState.Canceled)
        return true
    }
}