package com.example.timewarp.domain.usecase

import com.example.timewarp.data.repository.FakeNotificationRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [CancelReservationUseCase] (US 01.07.01, US 01.07.02).
 * Fake repositories are injected through the constructor, so no Firebase is needed.
 */
class CancelReservationUseCaseTest {

    private val userId = "entrant-1"

    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var cancelReservation: CancelReservationUseCase

    @Before
    fun setUp() {
        reservationRepository = FakeReservationRepository()
        notificationRepository = FakeNotificationRepository()
        reservationRepository.clear()
        notificationRepository.clear()

        // Inject the fake repositories, like Hilt would in the app
        cancelReservation = CancelReservationUseCase(reservationRepository, notificationRepository)
    }

    // Adds one reservation with the given state to the fake repository
    private fun addReservation(id: String, state: ReservationState) {
        val reservation = Reservation(reservationId = id, reservationState = state, userId = userId)
        reservationRepository.addReservation(reservation)
    }

    @Test
    fun bookedReservation_isCancelled() = runBlocking {
        addReservation("r1", ReservationState.Reserved)

        val result = cancelReservation("r1")

        assertTrue(result)
        assertEquals(ReservationState.Canceled, reservationRepository.getReservation("r1")?.reservationState)
    }

    @Test
    fun cancelledReservation_isKeptNotDeleted() = runBlocking {
        addReservation("r1", ReservationState.Reserved)

        cancelReservation("r1")

        assertEquals(1, reservationRepository.getReservations()?.size)
    }

    @Test
    fun alreadyCancelled_cannotCancelAgain() = runBlocking {
        addReservation("r1", ReservationState.Canceled)

        val result = cancelReservation("r1")

        assertFalse(result)
    }

    @Test
    fun rejectedReservation_staysRejected() = runBlocking {
        addReservation("r1", ReservationState.Rejected)

        val result = cancelReservation("r1")

        assertFalse(result)
        assertEquals(ReservationState.Rejected, reservationRepository.getReservation("r1")?.reservationState)
    }

    @Test
    fun missingReservation_returnsFalse() = runBlocking {
        val result = cancelReservation("does-not-exist")

        assertFalse(result)
    }

    @Test
    fun cancelling_sendsNotificationToEntrant() = runBlocking {
        addReservation("r1", ReservationState.Reserved)

        cancelReservation("r1")

        val received = notificationRepository.getIncomingNotifications(userId)
        assertEquals(1, received?.size)
        assertTrue(received!![0].notification.contains("r1"))
    }

    @Test
    fun failedCancel_sendsNoNotification() = runBlocking {
        addReservation("r1", ReservationState.Canceled)

        cancelReservation("r1")

        assertEquals(0, notificationRepository.getNotifications()?.size)
    }

    @Test
    fun cancelNotifications_haveUniqueIds() = runBlocking {
        addReservation("r1", ReservationState.Reserved)
        addReservation("r2", ReservationState.Reserved)

        cancelReservation("r1")
        cancelReservation("r2")

        val sent = notificationRepository.getNotifications()!!
        assertEquals(2, sent.size)
        assertTrue(sent[0].notificationId.isNotEmpty())
        assertNotEquals(sent[0].notificationId, sent[1].notificationId)
    }
}