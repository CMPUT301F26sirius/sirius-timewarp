package com.example.timewarp.domain.usecase

import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.repository.ReservationRepository
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of pollOnce
 */
data class LotteryDrawPollResult(
    val lotteriesChecked: Int,
    val winnersSelected: Int,
    val notSelectedNotified: Int,
)

/**
 * [LotteryDrawPollerUseCase]
 *
 * Iterates through lotteries, and draws when winner count exceeds active winner count and
 * the lottery registration period has ended.
 */
@Singleton
class LotteryDrawPollerUseCase @Inject constructor(
    private val lotteryRepository: LotteryRepository,
    private val reservationRepository: ReservationRepository,
    private val notificationRepository: NotificationRepository,
) {
    /**
     * Polls lotteries using [nowMillis], allowing tests to supply a fixed timestamp for testing.
     *
     * @param nowMillis
     * @return [LotteryDrawPollResult]
     */
    suspend fun pollOnce(nowMillis: Long = System.currentTimeMillis()): LotteryDrawPollResult {
        // Get all lotteries
        val lotteries = lotteryRepository.getLotteries() ?: throw IllegalStateException("Lottery repository returned no lottery list")
        var lotteriesChecked = 0
        var winnersSelected = 0
        var notSelectedNotified = 0

        // Iterate through lotteries
        for (lottery in lotteries) {
            val result = processLottery(lottery, nowMillis)
            lotteriesChecked += result.lotteriesChecked
            winnersSelected += result.winnersSelected
            notSelectedNotified += result.notSelectedNotified
        }
        return LotteryDrawPollResult(lotteriesChecked, winnersSelected, notSelectedNotified)
    }

    /**
     * Checks and draws winners for only the specified lottery.
     *
     * @param lotteryId
     * @param nowMillis
     * @return [LotteryDrawPollResult]
     */
    suspend fun pollLottery(
        lotteryId: String,
        nowMillis: Long = System.currentTimeMillis(),
    ): LotteryDrawPollResult {
        val lottery = lotteryRepository.getLottery(lotteryId)
            ?: throw IllegalStateException("Lottery $lotteryId was not found")
        return processLottery(lottery, nowMillis)
    }

    /**
     * Checks and draws winners for only the specified lottery.
     *
     * @param lottery
     * @param nowMillis
     * @return [LotteryDrawPollResult]
     */
    private suspend fun processLottery(lottery: Lottery, nowMillis: Long): LotteryDrawPollResult {
        // Do not draw if it is not past the registration period
        val today = DayFormatter.fromMillis(nowMillis)
        if (today <= lottery.registrationPeriod.end || lottery.winnerCount <= 0) { return LotteryDrawPollResult(0, 0, 0) }
        check(lottery.lotteryId.isNotBlank()) { "A due lottery has no ID" }

        // Check winner count
        var winnersSelected = 0
        var notSelectedNotified = 0
        var activeWinnerCount = reservationRepository.getOrInitializeActiveWinnerCount(lottery.lotteryId)
        if (activeWinnerCount < lottery.winnerCount) {
            // Active winner count is less than winner count, get candidates (waitlisted)
            val candidates = reservationRepository.getWaitlistedReservations(lottery.lotteryId).shuffled()

            // Iterate over shuffled candidates and attempt to promote
            for (candidate in candidates) {
                if (activeWinnerCount >= lottery.winnerCount) break

                // Winner notification
                val notification = winnerNotification(
                    lottery = lottery,
                    reservationId = candidate.reservationId,
                    userId = candidate.userId,
                    nowMillis = nowMillis,
                )

                // Attempt to promote the reservation
                if (reservationRepository.tryPromoteWaitlistedReservation(reservationId = candidate.reservationId, promoteNotification = notification)) {
                    activeWinnerCount++
                    winnersSelected++
                    notificationRepository.sendNotification(notification)
                }
            }
        }

        // If all winners are selected, send lost notification to remaining list of entrants
        if (activeWinnerCount >= lottery.winnerCount) {
            val remainingWaitlist = reservationRepository.getWaitlistedReservations(lottery.lotteryId)
            for (reservation in remainingWaitlist) {
                notificationRepository.sendNotification(
                    notSelectedNotification(
                        lotteryId = lottery.lotteryId,
                        userId = reservation.userId,
                        reservationId = reservation.reservationId,
                        nowMillis = nowMillis,
                    )
                )
                notSelectedNotified++
            }
        }

        return LotteryDrawPollResult(1, winnersSelected, notSelectedNotified)
    }

    // Helper to make a winner notification
    private fun winnerNotification(lottery: Lottery, reservationId: String, userId: String, nowMillis: Long): Notification {
        return Notification(
            timestamp = TimeUnit.MILLISECONDS.toSeconds(nowMillis).toInt(),
            notificationId = UUID.randomUUID().toString(),
            notification = "You won a lottery that you can reserve from ${DayFormatter.shortDate(lottery.bookingPeriod.start)} through ${DayFormatter.shortDate(lottery.bookingPeriod.end)}! Check your reservations tab! (Lottery: ${lottery.lotteryId})",
            fromUserId = CancelReservationUseCase.SYSTEM_SENDER,
            toUserId = userId
        )
    }

    // Helper to make a loser notification
    private fun notSelectedNotification(lotteryId: String, userId: String, reservationId: String, nowMillis: Long): Notification {
        return Notification(
            timestamp = TimeUnit.MILLISECONDS.toSeconds(nowMillis).toInt(),
            notificationId = UUID.randomUUID().toString(),
            notification = "You were not selected in this draw, but remain waitlisted and may be selected if a winner cancels or is rejected. (Lottery: ${lotteryId})",
            fromUserId = CancelReservationUseCase.SYSTEM_SENDER,
            toUserId = userId,
        )
    }
}
