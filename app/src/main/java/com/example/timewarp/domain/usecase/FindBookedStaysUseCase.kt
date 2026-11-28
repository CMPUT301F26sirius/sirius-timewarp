package com.example.timewarp.domain.usecase

import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ReservationRepository
import jakarta.inject.Inject

/*
 * Finds the booked stays at one campsite so its owner can see them (US 02.06.01).
 * A stay counts if it's Reserved, has saved dates, and hasn't ended yet.
 * Outstanding: loads every reservation and filters here, since the repository has no query by campsite.
 */

/**
 * One booked stay at a campsite.
 *
 * @property reservationId the reservation that booked it.
 * @property userId the entrant who booked it.
 * @property checkIn first night as a day number.
 * @property checkOut check-out day as a day number.
 */
data class BookedStay(
    val reservationId: String,
    val userId: String,
    val checkIn: Int,
    val checkOut: Int
)

/**
 * Lists the current and upcoming booked stays at a campsite, earliest first.
 * Days are day numbers (days since Jan 1, 1970).
 */
class FindBookedStaysUseCase @Inject constructor(
    private val reservationRepository: ReservationRepository
) {

    /**
     * Returns the stays booked at [campsiteId] that haven't ended before [today].
     *
     * @param campsiteId the campsite to look at.
     * @param today today's day number; stays that ended before today are left out.
     * @return booked stays sorted by check-in day.
     */
    suspend operator fun invoke(campsiteId: String, today: Int): List<BookedStay> {
        val stays = mutableListOf<BookedStay>()

        for (reservation in reservationRepository.getReservations().orEmpty()) {
            // Only reservations at this campsite
            if (reservation.campsiteId != campsiteId) {
                continue
            }
            // Only confirmed bookings take dates
            if (reservation.reservationState != ReservationState.Reserved) {
                continue
            }

            val checkIn = DayFormatter.fromDateString(reservation.datesBooked.startDate)
            val checkOut = DayFormatter.fromDateString(reservation.datesBooked.endDate)

            // Bookings without saved dates have nothing to show
            if (checkIn == null || checkOut == null) {
                continue
            }
            // Stay is already over; checking out today still counts as current
            if (checkOut < today) {
                continue
            }

            stays.add(
                BookedStay(
                    reservationId = reservation.reservationId,
                    userId = reservation.userId,
                    checkIn = checkIn,
                    checkOut = checkOut
                )
            )
        }

        // Earliest check-in first
        stays.sortBy { stay -> stay.checkIn }
        return stays
    }
}