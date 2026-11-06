package com.example.timewarp.domain.usecase

import com.example.timewarp.data.repository.FakeNotificationRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
* Unit tests for [ConfirmReservationUseCase] (US 01.05.01, US 01.05.02).
* Fake repositories are injected through the constructor, so no Firebase is needed.
 */
class ConfirmReservationUseCaseTest {

    private val userId = "entrant-1"
    private val campsite = Campsite(name = "Pocahontas", location = Location(address = "Jasper"))

    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var confirmReservation: ConfirmReservationUseCase

    @Before
    fun setUp() {
        reservationRepository = FakeReservationRepository()
        notificationRepository = FakeNotificationRepository()
        reservationRepository.clear()
        notificationRepository.clear()

        // Inject the fake repositories, like Hilt would in the app
        confirmReservation = ConfirmReservationUseCase(reservationRepository, notificationRepository)
    }

    // Adds one reservation with the given state to the fake repository
    private fun addReservation(id: String, state: ReservationState) {
        val reservation = Reservation(reservationId = id, reservationState = state, userId = userId)
        reservationRepository.addReservation(reservation)
    }

    @Test
    fun wonReservation_isReserved() = runBlocking {
        addReservation("r1", ReservationState.Pending)

        val result = confirmReservation("r1", campsite)

        assertTrue(result)
        assertEquals(ReservationState.Reserved, reservationRepository.getReservation("r1")?.reservationState)
    }

    @Test
    fun alreadyReserved_cannotConfirmAgain() = runBlocking {
        addReservation("r1", ReservationState.Reserved)

        val result = confirmReservation("r1", campsite)

        assertFalse(result)
    }

    @Test
    fun waitlisted_cannotConfirm() = runBlocking {
        addReservation("r1", ReservationState.Waitlisted)

        val result = confirmReservation("r1", campsite)

        assertFalse(result)
        assertEquals(ReservationState.Waitlisted, reservationRepository.getReservation("r1")?.reservationState)
    }

    @Test
    fun missingReservation_returnsFalse() = runBlocking {
        val result = confirmReservation("does-not-exist", campsite)

        assertFalse(result)
    }

    @Test
    fun confirming_sendsNotificationWithDetails() = runBlocking {
        addReservation("r1", ReservationState.Pending)

        confirmReservation("r1", campsite)

        val received = notificationRepository.getIncomingNotifications(userId)
        assertEquals(1, received?.size)
        assertTrue(received!![0].notification.contains("r1"))
        assertTrue(received[0].notification.contains("Pocahontas"))
        assertTrue(received[0].notificationId.isNotEmpty())
    }

    @Test
    fun failedConfirm_sendsNoNotification() = runBlocking {
        addReservation("r1", ReservationState.Canceled)

        confirmReservation("r1", campsite)

        assertEquals(0, notificationRepository.getNotifications()?.size)
    }
}