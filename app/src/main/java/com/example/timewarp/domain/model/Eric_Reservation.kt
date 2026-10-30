package com.example.timewarp.domain.model

import java.time.LocalDate
import kotlin.time.Instant

data class Eric_Reservation (
    val reservationId: String = "",
    val timestamp: Instant,
    val startDate: String,
    val endDate: String,
    val entrant: Eric_Entrant,
    val campsite: Campsite
)