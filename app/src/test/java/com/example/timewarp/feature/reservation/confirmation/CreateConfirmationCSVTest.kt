package com.example.timewarp.feature.reservation.confirmation

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import org.junit.Assert.assertTrue
import org.junit.Test


// Unit tests for [createConfirmationCSV] (US 01.05.02).
class CreateConfirmationCSVTest {

    private val reservation = Reservation(
        reservationId = "r1",
        reservationState = ReservationState.Reserved,
        userId = "entrant-1"
    )

    @Test
    fun csv_hasHeaderRow() {
        val csv = createConfirmationCSV(reservation, null)

        assertTrue(csv.contains("Reservation #,Status,Campsite,Location,Booked by"))
    }

    @Test
    fun csv_hasReservationAndCampsiteDetails() {
        val campsite = Campsite(name = "Pocahontas", location = Location(address = "Jasper"))

        val csv = createConfirmationCSV(reservation, campsite)

        assertTrue(csv.contains("r1,Reserved,Pocahontas,Jasper,entrant-1"))
    }

    @Test
    fun unknownCampsite_leavesCampsiteColumnsEmpty() {
        val csv = createConfirmationCSV(reservation, null)

        assertTrue(csv.contains("r1,Reserved,,,entrant-1"))
    }
}