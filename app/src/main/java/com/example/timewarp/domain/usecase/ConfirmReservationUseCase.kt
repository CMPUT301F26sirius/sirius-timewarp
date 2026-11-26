package com.example.timewarp.domain.usecase

import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Availability
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
 * Outstanding: doesn't check the entrant's notification opt-out.
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
     * @param campsite the chosen campsite; its id is saved on the reservation
     * and its name goes in the notification. Null if unknown.
     * @param checkIn first night of the stay as a day number, or null if no dates were picked.
     * @param checkOut check-out day as a day number, or null if no dates were picked.
     * @return true if it was confirmed, false if it doesn't exist or isn't Pending.
     */

    suspend operator fun invoke(
        reservationId: String,
        campsite: Campsite?,
        checkIn: Int? = null,
        checkOut: Int? = null
    ): Boolean {
        val reservation = reservationRepository.getReservation(reservationId)

        // Nothing to confirm
        if (reservation == null) {
            return false
        }

        // Already reserved, cancelled, waitlisted, etc.
        if (!canConfirm(reservation)) {
            return false
        }

        var confirmed = reservation.copy(reservationState = ReservationState.Reserved)

        // Save which of the lottery's campsites the winner chose
        if (campsite != null && campsite.campsiteId.isNotEmpty()) {
            confirmed = confirmed.copy(campsiteId = campsite.campsiteId)
        }

        if (checkIn != null && checkOut != null) {
            val dates = Availability(
                startDate = DayFormatter.toDateString(checkIn),
                endDate = DayFormatter.toDateString(checkOut)
            )
            confirmed = confirmed.copy(datesBooked = dates)
        }

        // Save the new state, campsite and dates in one write
        reservationRepository.editReservation(reservationId, confirmed)

        // Tell the entrant their booking went through (US 01.05.02)
        notificationRepository.sendNotification(buildConfirmationNotification(confirmed, campsite))

        return true
    }

    /**
     * Builds the confirmation notification for [reservation], including the campsite and dates.
     */
    fun buildConfirmationNotification(reservation: Reservation, campsite: Campsite?): Notification {
        // Fall back to generic text if the campsite couldn't be loaded
        var campsiteText = "your campsite"
        if (campsite != null) {
            campsiteText = "${campsite.name}, ${campsite.location.address}"
        }

        // Add the stay dates when they were picked
        var datesText = ""
        val start = reservation.datesBooked.startDate
        val end = reservation.datesBooked.endDate
        if (start.isNotEmpty() && end.isNotEmpty()) {
            datesText = " from $start to $end"
        }

        return Notification(
            notificationId = UUID.randomUUID().toString(),
            notification = "Your reservation #${reservation.reservationId} at $campsiteText$datesText is confirmed.",
            fromUserId = CancelReservationUseCase.SYSTEM_SENDER,
            toUserId = reservation.userId
        )
    }
}