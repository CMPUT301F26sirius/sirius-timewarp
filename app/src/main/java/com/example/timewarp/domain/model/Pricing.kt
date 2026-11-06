package com.example.timewarp.domain.model

/**
 * [Pricing] data class
 *
 * Pricing details for a campsite reservation.
 *
 * @property preparationFee - One-time preparation fee.
 * @property securityDeposit - Refundable security deposit amount.
 * @property costPerNight - Nightly campsite cost.
 * @property currencyCode - Currency type
 */
data class Pricing (
    val preparationFee: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val costPerNight: Double = 0.0,
    val currencyCode: String = "CAD"
)