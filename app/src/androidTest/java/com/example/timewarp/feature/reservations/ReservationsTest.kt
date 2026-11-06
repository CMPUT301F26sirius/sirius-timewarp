package com.example.timewarp.feature.reservations

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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/*
* Instrumented tests for the reservations screen that covers user stories
* 01.06.01 and 01.06.02.
* The test runs against fake repositories. It covers viewing active reservations
* (Booked and Booking tabs) and past reservations (Cancelled tab).
*/

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ReservationsTest {
    @get:
    Rule(order = 0) val hiltRule = HiltAndroidRule(this)

    @get:
    Rule(order = 1) val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var reservationRepository: ReservationRepository

    /*
    * This function builds a test reservation with the given id, user, and state
    */
    private fun reservation(
        id: String,
        userId: String,
        state: ReservationState
    ) =
        Reservation(
            reservationId = id,
            reservationState = state,
            campsiteId = "campsite",
            userId = userId
        )

    /*
    * This function logs a test entrant in and clears all reservations before each test
    */
    @Before
    fun setUp() {
        hiltRule.inject()
        (authRepository as FakeAuthRepository).setCurrentUserId("reservations-user")
        (userRepository as FakeUserRepository).addUser(
            User(userId = "reservations-user", userType = UserType.Entrant)
        )
        (reservationRepository as FakeReservationRepository).clear()
    }

    /*
    * This function waits for 5 seconds for the tag to appear on the screen
    */
    private fun waitForTag(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

    /*
    * This function opens the reservations tab from the bottom nav and waits for the tabs
    */
    private fun openReservations() {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Reservations").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Reservations").performClick()
        waitForTag("tab_Booked")
    }

    /*
    * US 01.06.01: This shows a reserved reservation on the Booked tab
    */
    @Test
    fun bookedTab_showsReserved() {
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(reservation("r1", "reservations-user", ReservationState.Reserved))
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        waitForTag("reservation_r1")
        composeRule.onNodeWithTag("reservation_r1").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.06.01: This shows waitlisted and pending reservations on the Booking tab
    */
    @Test
    fun bookingTab_showsWaitlistedAndPending() {
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(reservation("r1", "reservations-user", ReservationState.Waitlisted))
        repository.addReservation(reservation("r2", "reservations-user", ReservationState.Pending))
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        composeRule.onNodeWithTag("tab_Booking").performClick()
        waitForTag("reservation_r1")
        composeRule.onNodeWithTag("reservation_r1").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_r2").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.06.01: This shows only the signed in user's reservations, not other users'
    */
    @Test
    fun showsOnlyOwnReservations() {
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(reservation("mine", "reservations-user", ReservationState.Reserved))
        repository.addReservation(reservation("theirs", "someone-else", ReservationState.Reserved))
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        waitForTag("reservation_mine")
        composeRule.onNodeWithTag("reservation_mine").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_theirs").assertDoesNotExist()
        scenario.close()
    }

    /*
    * US 01.06.02: This shows canceled, declined and rejected reservations on the Cancelled tab
    */
    @Test
    fun showsCancelledReservations() {
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(reservation("r1", "reservations-user", ReservationState.Canceled))
        repository.addReservation(reservation("r2", "reservations-user", ReservationState.Declined))
        repository.addReservation(reservation("r3", "reservations-user", ReservationState.Rejected))
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        composeRule.onNodeWithTag("tab_Cancelled").performClick()
        waitForTag("reservation_r1")
        composeRule.onNodeWithTag("reservation_r1").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_r2").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_r3").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.06.02: This shows past reservations are kept apart from active ones
    */
    @Test
    fun past_cancelledReservation_notOnBookedTab() {
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(reservation("active", "reservations-user", ReservationState.Reserved))
        repository.addReservation(reservation("cancelled", "reservations-user", ReservationState.Canceled))
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        waitForTag("reservation_active")
        composeRule.onNodeWithTag("reservation_cancelled").assertDoesNotExist()
        scenario.close()
    }
}