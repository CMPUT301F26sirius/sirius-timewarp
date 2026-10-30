package com.example.timewarp.domain.model

data class Reservation (
    val reservationId: String = "",
    val reservationState: ReservationState = ReservationState.Waitlisted,

    val campsiteId: String = "",
    val userId: String = "",

    val datesBooked: Availability = Availability(),
    val pricingUsed: Pricing = Pricing()
)