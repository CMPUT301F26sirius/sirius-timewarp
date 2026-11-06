package com.example.timewarp.domain.model

/**
 * [Reservation] data class
 *
 * Campsite booking associated with a lottery and user. This can be in one
 * of many states, and indirectly allows us to query the wait list for a lottery.
 * Since each reservation points to a user and a lottery, we can query all reservations
 * for a given lottery. Reservations can be in the waitlist state.
 *
 * Issues:
 * - campsiteId, datesBooked, pricingUsed is clunky. Needs a refactor.
 *
 * @property reservationId - Unique identifier for the reservation.
 * @property reservationState - Current state of the reservation.
 * @property lotteryId - Identifier of the lottery that produced the reservation.
 * @property userId - Identifier of the reserving user.
 * @property timestamp - Reservation creation or update timestamp.
 * @property campsiteId - Identifier of the reserved campsite.
 * @property datesBooked - Dates included in the reservation.
 * @property pricingUsed - Pricing snapshot used for the reservation.
 */
data class Reservation (
    val reservationId: String = "",
    val reservationState: ReservationState = ReservationState.Waitlisted,

    val lotteryId: String = "",
    val userId: String = "",

    val timestamp: Int = 0,
    val campsiteId: String = "",
    val datesBooked: Availability = Availability(),
    val pricingUsed: Pricing = Pricing()
)
