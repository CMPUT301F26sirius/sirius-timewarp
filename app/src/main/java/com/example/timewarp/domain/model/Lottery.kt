package com.example.timewarp.domain.model

import com.google.firebase.firestore.ListenerRegistration

data class Lottery (
    val lotteryId: String = "",

    val registrationPeriod: Period = Period(),
    val bookingPeriod: Period = Period(),

    val winnerCount: Int = 1,
    val waitlistCapacity: Int = 100,

    val campsiteIds: List<String> = emptyList<String>()
)