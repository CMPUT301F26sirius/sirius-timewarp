package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ReservationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
* Unit tests for CancelReservationUseCase (US 01.07.01).
* Uses an in-memory fake repository, so no Firebase is needed.
*/

class CancelReservationUseCaseTest {

    private lateinit var repository: FakeReservationRepository
    private lateinit var cancelReservation: CancelReservationUseCase

    @Before
    fun setUp() {
        repository = FakeReservationRepository()
        cancelReservation = CancelReservationUseCase(repository)
    }

    // Adds one reservation with the given state to the fake database
    private fun addReservation(id: String, state: ReservationState) {
        val reservation = Reservation(reservationId = id, reservationState = state)
        repository.reservations.add(reservation)
    }

    @Test
    fun bookedReservation_isCancelled() = runBlocking {
        addReservation("r1", ReservationState.Reserved)

        val result = cancelReservation("r1")

        assertTrue(result)
        assertEquals(ReservationState.Canceled, repository.getReservation("r1")?.reservationState)
    }

    @Test
    fun cancelledReservation_isKeptNotDeleted() = runBlocking {
        addReservation("r1", ReservationState.Reserved)

        cancelReservation("r1")

        assertEquals(1, repository.reservations.size)
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
        assertEquals(ReservationState.Rejected, repository.getReservation("r1")?.reservationState)
    }

    @Test
    fun missingReservation_returnsFalse() = runBlocking {
        val result = cancelReservation("does-not-exist")

        assertFalse(result)
    }
}

// Fake "database" for the tests above: keeps reservations in a list
private class FakeReservationRepository : ReservationRepository {

    val reservations = mutableListOf<Reservation>()

    override suspend fun getReservations(): MutableList<Reservation>? {
        return reservations
    }

    override suspend fun createReservation(reservation: Reservation) {
        reservations.add(reservation)
    }

    override suspend fun editReservation(campsiteId: String, campsite: Reservation) {
        for (i in reservations.indices) {
            if (reservations[i].reservationId == campsite.reservationId) {
                reservations[i] = campsite
            }
        }
    }

    override suspend fun getReservation(reservationId: String): Reservation? {
        for (reservation in reservations) {
            if (reservation.reservationId == reservationId) {
                return reservation
            }
        }
        return null
    }

    override suspend fun updateReservationState(reservationId: String, state: ReservationState) {
        for (i in reservations.indices) {
            if (reservations[i].reservationId == reservationId) {
                // Replace with a copy that has the new state
                reservations[i] = reservations[i].copy(reservationState = state)
            }
        }
    }
}