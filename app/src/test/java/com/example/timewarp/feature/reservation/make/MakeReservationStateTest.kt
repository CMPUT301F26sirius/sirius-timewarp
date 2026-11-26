package com.example.timewarp.feature.reservation.make

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Pricing
import com.example.timewarp.domain.model.Reservation
import org.junit.Assert
import org.junit.Test

/*
 * Unit tests for the total cost on the Make reservation screen (US 01.04.01).
 */
class MakeReservationStateTest {

    // Christmas week 2027
    private val dec22 = 21174
    private val campsite =
        Campsite(campsiteId = "camp-1", name = "Pocahontas", pricing = Pricing(costPerNight = 45.0))

    // Screen state with the whole week bookable and the given dates picked
    private fun content(checkIn: Int?, checkOut: Int?): MakeReservationState.Content {
        return MakeReservationState.Content(
            reservation = Reservation(reservationId = "r1"),
            campsite = campsite,
            canConfirm = true,
            campsites = listOf(campsite),
            stayWindow = Period(start = dec22, end = dec22 + 6),
            bookableDays = (dec22..dec22 + 6).toList(),
            checkIn = checkIn,
            checkOut = checkOut
        )
    }

    @Test
    fun twoNights_costTwiceThePrice() {
        // Christmas Eve to Boxing Day is 2 nights
        val state = content(dec22 + 2, dec22 + 4)

        Assert.assertEquals(90.0, state.totalCost)
    }

    @Test
    fun noDates_noTotal() {
        val state = content(null, null)

        Assert.assertNull(state.totalCost)
    }

    @Test
    fun zeroNights_noTotal() {
        // Same check-in and check-out day isn't a stay
        val state = content(dec22 + 2, dec22 + 2)

        Assert.assertNull(state.totalCost)
    }
}