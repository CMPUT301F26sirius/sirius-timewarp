package com.example.timewarp.domain.usecase

import com.example.timewarp.data.repository.FakeNotificationRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
 * Unit tests for [RejectReservationUseCase] (US 02.08.01).
 * Fake repositories are injected through the constructor, so no Firebase is needed.
 */
class RejectReservationUseCaseTest {

    private val userId = "entrant-1"
    private val campsite = Campsite(campsiteId = "camp-1", name = "Pocahontas")

    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var rejectReservation: RejectReservationUseCase

    @Before
    fun setUp() {
        reservationRepository = FakeReservationRepository()
        notificationRepository = FakeNotificationRepository()
        reservationRepository.clear()
        notificationRepository.clear()

        // Inject the fake repositories, like Hilt would in the app
        rejectReservation = RejectReservationUseCase(reservationRepository, notificationRepository)
    }

    // Adds one reservation with the given state to the fake repository
    private fun addReservation(id: String, state: ReservationState) {
        val reservation = Reservation(reservationId = id, reservationState = state, userId = userId)
        reservationRepository.addReservation(reservation)
    }

    @Test
    fun wonReservation_isRejected() = runBlocking {
        addReservation("r1", ReservationState.Pending)

        val result = rejectReservation("r1", campsite)

        assertTrue(result)
        assertEquals(
            ReservationState.Rejected,
            reservationRepository.getReservation("r1")?.reservationState
        )
    }

    @Test
    fun bookedReservation_isRejected() = runBlocking {
        addReservation("r1", ReservationState.Reserved)

        val result = rejectReservation("r1", campsite)

        assertTrue(result)
        assertEquals(
            ReservationState.Rejected,
            reservationRepository.getReservation("r1")?.reservationState
        )
    }

    @Test
    fun waitlisted_cannotReject() = runBlocking {
        addReservation("r1", ReservationState.Waitlisted)

        val result = rejectReservation("r1", campsite)

        assertFalse(result)
        assertEquals(
            ReservationState.Waitlisted,
            reservationRepository.getReservation("r1")?.reservationState
        )
    }

    @Test
    fun cancelled_cannotReject() = runBlocking {
        addReservation("r1", ReservationState.Canceled)

        val result = rejectReservation("r1", campsite)

        assertFalse(result)
    }

    @Test
    fun rejecting_notifiesEntrantWithCampsite() = runBlocking {
        addReservation("r1", ReservationState.Pending)

        rejectReservation("r1", campsite)

        // One notification to the entrant, naming the campsite
        val received = notificationRepository.getIncomingNotifications(userId)
        assertEquals(1, received?.size)
        assertTrue(received!![0].notification.contains("Pocahontas"))
    }

    @Test
    fun cannotReject_sendsNoNotification() = runBlocking {
        addReservation("r1", ReservationState.Waitlisted)

        rejectReservation("r1", campsite)

        assertEquals(0, notificationRepository.getNotifications()?.size)
    }
}