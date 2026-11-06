package com.example.timewarp.domain.model

/**
 * Lifecycle states available for a reservation.
 */
enum class ReservationState {
    Waitlisted,
    Pending,
    Reserved,
    Declined,
    Canceled,
    Rejected,
}