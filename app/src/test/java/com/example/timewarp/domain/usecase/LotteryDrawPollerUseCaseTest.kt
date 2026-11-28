package com.example.timewarp.domain.usecase

import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeNotificationRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LotteryDrawPollerUseCaseTest {
    private lateinit var lotteryRepository: FakeLotteryRepository
    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var poller: LotteryDrawPollerUseCase

    @Before
    fun setUp() {
        lotteryRepository = FakeLotteryRepository()
        reservationRepository = FakeReservationRepository()
        notificationRepository = FakeNotificationRepository()
        poller = LotteryDrawPollerUseCase(
            lotteryRepository,
            reservationRepository,
            notificationRepository,
        )
    }

    @Test
    fun doesNotDrawUntilRegistrationPeriodHasEnded() = runBlocking {
        lotteryRepository.addLottery(lottery(winnerCount = 1))
        reservationRepository.addReservation(waitlisted("entrant-1"))

        val atRegistrationEnd = poller.pollOnce(DayFormatter.toMillis(REGISTRATION_END))
        assertEquals(0, atRegistrationEnd.winnersSelected)
        assertEquals(
            ReservationState.Waitlisted,
            reservationRepository.getReservation("entrant-1")?.reservationState,
        )

        val afterRegistrationEnd =
            poller.pollOnce(DayFormatter.toMillis(REGISTRATION_END + 1))
        assertEquals(1, afterRegistrationEnd.winnersSelected)
        assertEquals(
            ReservationState.Pending,
            reservationRepository.getReservation("entrant-1")?.reservationState,
        )
        val winnerNotification = notificationRepository.getNotifications().orEmpty().single()
        assertEquals("user-entrant-1", winnerNotification.toUserId)
        assertTrue(winnerNotification.notification.contains("You won a lottery"))
        assertTrue(winnerNotification.notification.contains("(Lottery: lottery-1)"))
    }

    @Test
    fun selectsOnlyAvailableWinnersAndDrawsReplacementAfterCancellation() = runBlocking {
        lotteryRepository.addLottery(lottery(winnerCount = 2))
        repeat(4) { reservationRepository.addReservation(waitlisted("entrant-$it")) }

        poller.pollOnce(DayFormatter.toMillis(REGISTRATION_END + 1))
        var reservations = reservationRepository.getReservations().orEmpty()
        assertEquals(2, reservations.count { it.reservationState == ReservationState.Pending })
        assertEquals(2, reservations.count { it.reservationState == ReservationState.Waitlisted })
        assertEquals(
            2,
            notificationRepository.getNotifications().orEmpty().count {
                it.notification.contains("You were not selected")
            },
        )

        val canceledWinner = reservations.first { it.reservationState == ReservationState.Pending }
        reservationRepository.updateReservationState(
            canceledWinner.reservationId,
            ReservationState.Canceled,
        )

        val replacementResult = poller.pollOnce(DayFormatter.toMillis(REGISTRATION_END + 2))
        reservations = reservationRepository.getReservations().orEmpty()
        assertEquals(1, replacementResult.winnersSelected)
        assertEquals(2, reservations.count { it.reservationState == ReservationState.Pending })
        assertEquals(1, reservations.count { it.reservationState == ReservationState.Waitlisted })
        assertEquals(
            3,
            notificationRepository.getNotifications().orEmpty().count {
                it.notification.contains("You won a lottery")
            },
        )
        assertTrue(
            notificationRepository.getNotifications().orEmpty().any {
                it.notification.contains("remain waitlisted")
            }
        )

        val rejectedWinner = reservations.first {
            it.reservationState == ReservationState.Pending &&
                it.reservationId != canceledWinner.reservationId
        }
        reservationRepository.updateReservationState(
            rejectedWinner.reservationId,
            ReservationState.Rejected,
        )

        val nextReplacement = poller.pollOnce(DayFormatter.toMillis(REGISTRATION_END + 3))
        reservations = reservationRepository.getReservations().orEmpty()
        assertEquals(1, nextReplacement.winnersSelected)
        assertEquals(2, reservations.count { it.reservationState == ReservationState.Pending })
        assertEquals(0, reservations.count { it.reservationState == ReservationState.Waitlisted })
        assertEquals(
            4,
            notificationRepository.getNotifications().orEmpty().count {
                it.notification.contains("You won a lottery")
            },
        )
    }

    @Test
    fun reservedEntriesCountTowardTheWinnerLimit() = runBlocking {
        lotteryRepository.addLottery(lottery(winnerCount = 1))
        reservationRepository.addReservation(
            waitlisted("already-reserved").copy(
                reservationState = ReservationState.Reserved,
            )
        )
        reservationRepository.addReservation(waitlisted("waiting"))

        val result = poller.pollOnce(DayFormatter.toMillis(REGISTRATION_END + 1))

        assertEquals(0, result.winnersSelected)
        assertEquals(
            ReservationState.Waitlisted,
            reservationRepository.getReservation("waiting")?.reservationState,
        )
    }

    @Test
    fun pendingEntriesCountTowardTheWinnerLimit() = runBlocking {
        lotteryRepository.addLottery(lottery(winnerCount = 1))
        reservationRepository.addReservation(
            waitlisted("pending").copy(
                reservationState = ReservationState.Pending,
            )
        )
        reservationRepository.addReservation(waitlisted("waiting"))

        val result = poller.pollOnce(DayFormatter.toMillis(REGISTRATION_END + 1))

        assertEquals(0, result.winnersSelected)
        assertEquals(
            ReservationState.Waitlisted,
            reservationRepository.getReservation("waiting")?.reservationState,
        )
    }

    @Test
    fun pollLotteryOnlyDrawsTheRequestedLottery() = runBlocking {
        val requestedLottery = lottery(winnerCount = 1)
        val otherLottery = lottery(winnerCount = 1).copy(lotteryId = "lottery-2")
        lotteryRepository.addLottery(requestedLottery)
        lotteryRepository.addLottery(otherLottery)
        reservationRepository.addReservation(waitlisted("entrant-1"))
        reservationRepository.addReservation(
            waitlisted("entrant-2").copy(lotteryId = otherLottery.lotteryId)
        )

        val result = poller.pollLottery(
            requestedLottery.lotteryId,
            DayFormatter.toMillis(REGISTRATION_END + 1),
        )

        assertEquals(1, result.lotteriesChecked)
        assertEquals(1, result.winnersSelected)
        assertEquals(
            ReservationState.Pending,
            reservationRepository.getReservation("entrant-1")?.reservationState,
        )
        assertEquals(
            ReservationState.Waitlisted,
            reservationRepository.getReservation("entrant-2")?.reservationState,
        )
    }

    private fun lottery(winnerCount: Int) = Lottery(
        lotteryId = "lottery-1",
        registrationPeriod = Period(start = REGISTRATION_END - 2, end = REGISTRATION_END),
        bookingPeriod = Period(start = REGISTRATION_END + 1, end = REGISTRATION_END + 5),
        winnerCount = winnerCount,
    )

    private fun waitlisted(id: String) = Reservation(
        reservationId = id,
        reservationState = ReservationState.Waitlisted,
        lotteryId = "lottery-1",
        userId = "user-$id",
    )

    companion object {
        const val REGISTRATION_END = 20_000
    }
}
