package com.example.timewarp.feature.reservation.details

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/*
* UI tests for the Manage reservation screen's cancel flow (US 01.07.01).
* Tests ReservationDetailsContent directly, so no Firebase is needed.
*/

class ReservationDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Builds the screen state for a reservation in the given state
    private fun contentState(state: ReservationState, canCancel: Boolean): ReservationDetailsState {
        val reservation = Reservation(reservationId = "PT-271204", reservationState = state)
        val campsite = Campsite(name = "Wapiti Camp", location = Location(address = "Jasper, Alberta"))
        return ReservationDetailsState.Content(reservation, campsite, canCancel)
    }

    @Test
    fun confirmingDialog_cancelsReservation() {
        var cancelCalled = false
        composeRule.setContent {
            ReservationDetailsContent(
                state = contentState(ReservationState.Reserved, canCancel = true),
                onCancelConfirmed = { cancelCalled = true }
            )
        }

        // Tap Cancel Booking, check the dialog opened, then confirm
        composeRule.onNodeWithTag("cancel_booking_button").performClick()
        composeRule.onNodeWithText("Cancel booking?").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm_cancel_button").performClick()

        assertTrue(cancelCalled)
    }

    @Test
    fun keepBooking_doesNotCancel() {
        var cancelCalled = false
        composeRule.setContent {
            ReservationDetailsContent(
                state = contentState(ReservationState.Reserved, canCancel = true),
                onCancelConfirmed = { cancelCalled = true }
            )
        }

        // Open the dialog, then back out
        composeRule.onNodeWithTag("cancel_booking_button").performClick()
        composeRule.onNodeWithText("Keep booking").performClick()

        assertFalse(cancelCalled)
    }

    @Test
    fun cancelledReservation_hasNoCancelButton() {
        composeRule.setContent {
            ReservationDetailsContent(
                state = contentState(ReservationState.Canceled, canCancel = false),
                onCancelConfirmed = {}
            )
        }

        composeRule.onNodeWithTag("cancel_booking_button").assertDoesNotExist()
        composeRule.onNodeWithText("This reservation can't be cancelled.").assertIsDisplayed()
    }
}