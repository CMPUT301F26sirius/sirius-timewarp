package com.example.timewarp.feature.reservation.cancellation

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.feature.reservation.cancellation.cancellationCSV
import org.junit.Assert
import org.junit.Test

class CancellationCSVTest {

    private val reservation = Reservation(
        reservationId = "r1",
        reservationState = ReservationState.Canceled,
        userId = "entrant-1"
    )

    @Test
    fun csv_hasHeaderRow() {
        val csv = cancellationCSV(reservation, null)

        Assert.assertTrue(csv.contains("Reservation #,Status,Campsite,Location,Booked by"))
    }

    @Test
    fun csv_hasReservationAndCampsiteDetails() {
        val campsite = Campsite(name = "Wapiti Camp", location = Location(address = "Jasper"))

        val csv = cancellationCSV(reservation, campsite)

        Assert.assertTrue(csv.contains("r1,Canceled,Wapiti Camp,Jasper,entrant-1"))
    }

    @Test
    fun unknownCampsite_leavesCampsiteColumnsEmpty() {
        val csv = cancellationCSV(reservation, null)

        Assert.assertTrue(csv.contains("r1,Canceled,,,entrant-1"))
    }
}