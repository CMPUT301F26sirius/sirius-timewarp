package com.example.timewarp.domain.model

data class Availability (
    val temp: String = "",

    val startDate: String = "",
    val endDate: String = "",
    val daysOfWeek: List<Int> = (1..7).toList()
)