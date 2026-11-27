package com.example.timewarp.domain.usecase

import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
 * Unit tests for [DeclineReservationUseCase] (US 01.04.03).
 * Fake repositories are injected through the constructor, so no Firebase is needed.
 */
class DeclineReservationUseCaseTest {

    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var declineReservation: DeclineReservationUseCase

    @Before
    fun setUp() {
        reservationRepository = FakeReservationRepository()
        reservationRepository.clear()

        // Inject the fake repository, like Hilt would in the app
        declineReservation = DeclineReservationUseCase(reservationRepository)
    }

    // Adds one reservation with the given state to the fake repository
    private fun addReservation(id: String, state: ReservationState) {
        val reservation =
            Reservation(reservationId = id, reservationState = state, userId = "entrant-1")
        reservationRepository.addReservation(reservation)
    }

    @Test
    fun wonReservation_isDeclined() = runBlocking {
        addReservation("r1", ReservationState.Pending)

        val result = declineReservation("r1")

        assertTrue(result)
        assertEquals(
            ReservationState.Declined,
            reservationRepository.getReservation("r1")?.reservationState
        )
    }

    @Test
    fun reserved_cannotDecline() = runBlocking {
        addReservation("r1", ReservationState.Reserved)

        val result = declineReservation("r1")

        // Booked stays are cancelled, not declined, so the state stays the same
        assertFalse(result)
        assertEquals(
            ReservationState.Reserved,
            reservationRepository.getReservation("r1")?.reservationState
        )
    }

    @Test
    fun waitlisted_cannotDecline() = runBlocking {
        addReservation("r1", ReservationState.Waitlisted)

        val result = declineReservation("r1")

        assertFalse(result)
        assertEquals(
            ReservationState.Waitlisted,
            reservationRepository.getReservation("r1")?.reservationState
        )
    }

    @Test
    fun missingReservation_returnsFalse() = runBlocking {
        val result = declineReservation("does-not-exist")

        assertFalse(result)
    }
}