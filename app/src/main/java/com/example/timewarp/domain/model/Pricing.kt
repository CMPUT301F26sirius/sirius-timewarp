package com.example.timewarp.domain.model

data class Pricing (
    val preparationFee: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val costPerNight: Double = 0.0,
    val currencyCode: String = "CAD"
)