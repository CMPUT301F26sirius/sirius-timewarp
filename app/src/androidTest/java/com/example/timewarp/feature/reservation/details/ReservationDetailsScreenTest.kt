package com.example.timewarp.feature.reservation.details

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.data.repository.FakeNotificationRepository
import com.example.timewarp.domain.repository.NotificationRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumented tests for cancelling a reservation (US 01.07.01).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ReservationDetailsScreenTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var reservationRepository: ReservationRepository

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val userId = "test-entrant"

    // ID opened by the temporary "Open test reservation" button
    private val reservationId = "0"

    @Before
    fun setUp() {
        hiltRule.inject()

        // Signed in as an entrant, so the app opens on Browse
        (authRepository as FakeAuthRepository).setCurrentUserId(userId)
        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(User(userId = userId, userType = UserType.Entrant))
        (reservationRepository as FakeReservationRepository).clear()
        (notificationRepository as FakeNotificationRepository).clear()
    }

    // Adds the test reservation with the given state
    private fun addReservation(state: ReservationState) {
        val reservation = Reservation(reservationId = reservationId, reservationState = state, userId = userId)
        (reservationRepository as FakeReservationRepository).addReservation(reservation)
    }

    // Launches the app and opens the Manage reservation screen
    private fun openReservationDetails(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/BROWSE").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Reservations").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Open test reservation").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Open test reservation").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/RESERVATION/DETAILS").fetchSemanticsNodes().isNotEmpty()
        }
        return scenario
    }

    @Test
    fun confirmingCancel_opensCancellationScreen_andCancelsReservation() {
        addReservation(ReservationState.Reserved)
        val scenario = openReservationDetails()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("cancel_booking_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("cancel_booking_button").performClick()
        composeRule.onNodeWithTag("confirm_cancel_button").performClick()

        // Cancellation screen appears
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("cancellation_screen").fetchSemanticsNodes().isNotEmpty()
        }

        // Reservation was cancelled, not deleted
        val state = runBlocking { reservationRepository.getReservation(reservationId)?.reservationState }
        assertEquals(ReservationState.Canceled, state)

        // Confirmation CSV button and notification (US 01.07.02)
        composeRule.onNodeWithTag("download_csv_button").assertIsDisplayed()
        val notifications = runBlocking { notificationRepository.getIncomingNotifications(userId) }
        assertEquals(1, notifications?.size)

        scenario.close()
    }

    @Test
    fun keepBooking_doesNotCancel() {
        addReservation(ReservationState.Reserved)
        val scenario = openReservationDetails()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("cancel_booking_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("cancel_booking_button").performClick()
        composeRule.onNodeWithText("Keep booking").performClick()

        composeRule.onNodeWithTag("cancel_booking_button").assertIsDisplayed()
        val state = runBlocking { reservationRepository.getReservation(reservationId)?.reservationState }
        assertEquals(ReservationState.Reserved, state)

        scenario.close()
    }

    @Test
    fun cancelledReservation_hasNoCancelButton() {
        addReservation(ReservationState.Canceled)
        val scenario = openReservationDetails()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("This reservation can't be cancelled.").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("cancel_booking_button").assertDoesNotExist()

        scenario.close()
    }
}