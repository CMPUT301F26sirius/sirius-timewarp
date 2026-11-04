package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.repository.ReservationRepository
import jakarta.inject.Inject

/*
 * Cancels an entrant's reservation (US 01.07.01) and notifies them (US 01.07.02).
 * Outstanding: doesn't block cancelling a stay that already happened (dates not stored yet);
 * doesn't check notification opt-out (US 01.10.01, no setting on User yet).
 */

/**
 * Cancels a reservation by setting its state to Canceled, then sends the entrant a notification.
 * The reservation is kept, not deleted.
 */
class CancelReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val notificationRepository: NotificationRepository
) {

    /** Returns true if [reservation] can be cancelled. Only Reserved ones can. */
    fun canCancel(reservation: Reservation): Boolean {
        if (reservation.reservationState == ReservationState.Reserved) {
            return true
        }
        return false
    }

    /**
     * Cancels the reservation with [reservationId] and notifies its entrant.
     *
     * @return true if it was cancelled, false if it doesn't exist or can't be cancelled.
     */
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

        // Confirm the cancellation to the entrant (US 01.07.02)
        notificationRepository.sendNotification(buildCancellationNotification(reservation))
        return true
    }

    /**
     * Builds the confirmation notification for a cancelled [reservation].
     */
    fun buildCancellationNotification(reservation: Reservation): Notification {
        return Notification(
            notification = "Your reservation #${reservation.reservationId} has been cancelled.",
            fromUserId = SYSTEM_SENDER,
            toUserId = reservation.userId
        )
    }

    companion object {
        /** Sender ID used for notifications the app sends automatically. */
        const val SYSTEM_SENDER = "system"
    }
}