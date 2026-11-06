package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.repository.ReservationRepository
import jakarta.inject.Inject
import java.util.UUID

/*
* Confirms a lottery winner's reservation (US 01.05.01) and notifies them (US 01.05.02).
* Outstanding: no dates picked yet (US 01.04.01) so it doesn't check notification opt-out.
 */

/**
 * Confirms a won reservation by changing its state from Pending to Reserved,
 * then sends the entrant a notification with the reservation details.
 */
class ConfirmReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val notificationRepository: NotificationRepository
) {

    /** Returns true if [reservation] can be confirmed. Only Pending (won) ones can. */
    fun canConfirm(reservation: Reservation): Boolean {
        if (reservation.reservationState == ReservationState.Pending) {
            return true
        }
        return false
    }

    /**
     * Confirms the reservation with [reservationId] and notifies its entrant.
     *
     * @param campsite the reserved campsite, used in the notification text; null if unknown.
     * @return true if it was confirmed, false if it doesn't exist or isn't Pending.
     */
    suspend operator fun invoke(reservationId: String, campsite: Campsite?): Boolean {
        val reservation = reservationRepository.getReservation(reservationId)

        if (reservation == null) {
            return false
        }

        if (!canConfirm(reservation)) {
            return false
        }

        reservationRepository.updateReservationState(reservationId, ReservationState.Reserved)

        // Tell the entrant their booking went through (US 01.05.02)
        notificationRepository.sendNotification(buildConfirmationNotification(reservation, campsite))
        return true
    }

    /**
     * Builds the confirmation notification for [reservation], including the campsite details.
     */
    fun buildConfirmationNotification(reservation: Reservation, campsite: Campsite?): Notification {
        var campsiteText = "your campsite"
        if (campsite != null) {
            campsiteText = "${campsite.name}, ${campsite.location.address}"
        }

        return Notification(
            notificationId = UUID.randomUUID().toString(),
            notification = "Your reservation #${reservation.reservationId} at $campsiteText is confirmed.",
            fromUserId = CancelReservationUseCase.SYSTEM_SENDER,
            toUserId = reservation.userId
        )
    }
}