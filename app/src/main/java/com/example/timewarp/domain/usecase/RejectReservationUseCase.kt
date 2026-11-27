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
 * Lets a campsite owner reject a reservation at their campsite (US 02.08.01) and notifies the entrant.
 * Outstanding: the new winner is rolled by the auto-draw, which hooks into the state change.
 * Rejects a reservation by changing its state to Rejected, then sends the entrant a notification.
 * The reservation is kept, not deleted.
 */
class RejectReservationUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val notificationRepository: NotificationRepository
) {

    /** Returns true if [reservation] can be rejected: won (Pending) or booked (Reserved). */
    fun canReject(reservation: Reservation): Boolean {
        if (reservation.reservationState == ReservationState.Pending) {
            return true
        }
        if (reservation.reservationState == ReservationState.Reserved) {
            return true
        }
        return false
    }

    /**
     * Rejects the reservation with [reservationId] and notifies its entrant.
     *
     * @param campsite the reservation's campsite, used in the notification text; null if unknown.
     * @return true if it was rejected, false if it doesn't exist or can't be rejected.
     */
    suspend operator fun invoke(reservationId: String, campsite: Campsite?): Boolean {
        val reservation = reservationRepository.getReservation(reservationId)

        // Nothing to reject
        if (reservation == null) {
            return false
        }

        // Waitlisted, cancelled, declined, or already rejected
        if (!canReject(reservation)) {
            return false
        }

        // Changing the state through the repository lets the auto-draw roll a new winner
        reservationRepository.updateReservationState(reservationId, ReservationState.Rejected)

        // Tell the entrant the owner rejected it
        notificationRepository.sendNotification(buildRejectionNotification(reservation, campsite))
        return true
    }

    /**
     * Builds the notification telling the entrant their [reservation] was rejected.
     */
    fun buildRejectionNotification(reservation: Reservation, campsite: Campsite?): Notification {
        // Fall back to generic text if the campsite couldn't be loaded
        var campsiteText = "your campsite"
        if (campsite != null) {
            campsiteText = campsite.name
        }

        return Notification(
            notificationId = UUID.randomUUID().toString(),
            notification = "Your reservation #${reservation.reservationId} at $campsiteText was rejected by the campsite owner.",
            fromUserId = CancelReservationUseCase.SYSTEM_SENDER,
            toUserId = reservation.userId
        )
    }
}