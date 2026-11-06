package com.example.timewarp.domain.model

/**
 * [Location] data class
 *
 * Geographic location of a campsite. Stores the human-readable address and
 * geographic coordinates used to identify the campsite.
 *
 * @property address - Human-readable street or regional address.
 * @property latitude - Latitude in decimal degrees.
 * @property longitude - Longitude in decimal degrees.
 */
data class Location(
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)