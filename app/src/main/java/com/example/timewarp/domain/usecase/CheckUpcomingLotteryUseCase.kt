package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Lottery
import jakarta.inject.Inject

/*
 * Decides whether a campsite has a lottery that is open or still to come (US 02.03.01).
 * Days are day numbers (days since Jan 1, 1970), like Period.
 */

/** Checks if any lottery is still open for registration or hasn't started yet. */
class CheckUpcomingLotteryUseCase @Inject constructor() {

    /** Returns true if at least one of [lotteries] has a registration period that ends on [today] or later. */
    operator fun invoke(lotteries: List<Lottery>, today: Int): Boolean {
        for (lottery in lotteries) {
            if (lottery.registrationPeriod.end >= today) {
                return true
            }
        }
        return false
    }
}