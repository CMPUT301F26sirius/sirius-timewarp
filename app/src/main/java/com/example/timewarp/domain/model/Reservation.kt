package com.example.timewarp.domain.model

data class Reservation (
    val reservationId: String = "",
    val reservationState: ReservationState = ReservationState.Waitlisted,
    val timestamp: Int = 0,

    val campsiteId: String = "", // The selected campsite
    val lotteryId: String = "",
    val userId: String = "",

    val datesBooked: Availability = Availability(),
    val pricingUsed: Pricing = Pricing()
)
