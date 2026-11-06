package com.example.timewarp.domain.model

/**
 * [Lottery] data class
 *
 * Lottery configuration for one or more campsites. Lottery records are
 * stored in the Firebase `Lottery` collection.
 *
 * @property lotteryId - Unique identifier for the lottery.
 * @property registrationPeriod - Period during which entrants can register.
 * @property bookingPeriod - Period during which winners can book.
 * @property winnerCount - Number of winners selected by the lottery.
 * @property waitlistCapacity - Maximum number of waitlisted entrants.
 * @property campsiteIds - Identifiers of campsites included in the lottery.
 */
data class Lottery (
    val lotteryId: String = "",

    val registrationPeriod: Period = Period(),
    val bookingPeriod: Period = Period(),

    val winnerCount: Int = 1,
    val waitlistCapacity: Int = 100,

    val campsiteIds: List<String> = emptyList<String>()
)