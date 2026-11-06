package com.example.timewarp.domain.model

/**
 * [Occupancy] data class
 *
 * Occupancy limits for a [Campsite].
 *
 * @property maxGuests - Maximum number of guests allowed at the campsite.
 */
data class Occupancy (
    val maxGuests: Int = 1,
)