package com.example.timewarp.domain.model

import java.time.LocalDate
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

data class Reservation @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = "",
    val timestamp: Instant,
    val startDate: String,
    val endDate: String,
    val entrant: Entrant,
    val campsite: Campsite,

