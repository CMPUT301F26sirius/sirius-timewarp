package com.example.timewarp.domain.model

data class Reservation (
    val reservationId: String = "",
    val reservationState: ReservationState = ReservationState.Waitlisted,

    val lotteryId: String = "",
    val userId: String = "",

    // Probably want to refactor this into a subtype/history of states
    val timestamp: Int = 0,
    val campsiteId: String = "", // Selected Campsite
    val datesBooked: Availability = Availability(),
    val pricingUsed: Pricing = Pricing()
)
