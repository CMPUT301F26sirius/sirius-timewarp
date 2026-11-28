package com.example.timewarp.domain.usecase

import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.domain.model.Availability
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
 * Unit tests for [FindBookedStaysUseCase] (US 02.06.01).
 * Dates are around Christmas 2027; "today" is Dec 22, 2027.
 */
class FindBookedStaysUseCaseTest {

    private val campsiteId = "camp-1"
    private val dec22 = 21174

    private lateinit var reservationRepository: FakeReservationRepository
    private lateinit var findBookedStays: FindBookedStaysUseCase

    @Before
    fun setUp() {
        reservationRepository = FakeReservationRepository()
        reservationRepository.clear()

        // Inject the fake repository, like Hilt would in the app
        findBookedStays = FindBookedStaysUseCase(reservationRepository)
    }

    // Adds a reservation at a campsite with the given state and stored dates
    private fun addReservation(
        id: String,
        state: ReservationState,
        start: String,
        end: String,
        atCampsite: String = campsiteId
    ) {
        val reservation = Reservation(
            reservationId = id,
            reservationState = state,
            userId = "entrant-$id",
            campsiteId = atCampsite,
            datesBooked = Availability(startDate = start, endDate = end)
        )
        reservationRepository.addReservation(reservation)
    }

    @Test
    fun reservedStay_isListed() = runBlocking {
        addReservation("r1", ReservationState.Reserved, "2027-12-24", "2027-12-26")

        val stays = findBookedStays(campsiteId, dec22)

        assertEquals(1, stays.size)
        assertEquals(dec22 + 2, stays[0].checkIn)
        assertEquals(dec22 + 4, stays[0].checkOut)
    }

    @Test
    fun otherCampsite_notListed() = runBlocking {
        addReservation("r1", ReservationState.Reserved, "2027-12-24", "2027-12-26", atCampsite = "camp-2")

        val stays = findBookedStays(campsiteId, dec22)

        assertTrue(stays.isEmpty())
    }

    @Test
    fun pendingAndCancelled_notListed() = runBlocking {
        addReservation("r1", ReservationState.Pending, "2027-12-24", "2027-12-26")
        addReservation("r2", ReservationState.Canceled, "2027-12-24", "2027-12-26")

        val stays = findBookedStays(campsiteId, dec22)

        assertTrue(stays.isEmpty())
    }

    @Test
    fun reservedWithoutDates_notListed() = runBlocking {
        addReservation("r1", ReservationState.Reserved, "", "")

        val stays = findBookedStays(campsiteId, dec22)

        assertTrue(stays.isEmpty())
    }

    @Test
    fun pastStay_notListed() = runBlocking {
        // Ended Dec 21, the day before "today"
        addReservation("r1", ReservationState.Reserved, "2027-12-19", "2027-12-21")

        val stays = findBookedStays(campsiteId, dec22)

        assertTrue(stays.isEmpty())
    }

    @Test
    fun stays_sortedByCheckIn() = runBlocking {
        // Added out of order on purpose
        addReservation("later", ReservationState.Reserved, "2027-12-27", "2027-12-29")
        addReservation("earlier", ReservationState.Reserved, "2027-12-23", "2027-12-25")

        val stays = findBookedStays(campsiteId, dec22)

        assertEquals("earlier", stays[0].reservationId)
        assertEquals("later", stays[1].reservationId)
    }
}