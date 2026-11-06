package com.example.timewarp.feature.reservation.make

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeNotificationRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumented tests for confirming a won reservation (US 01.05.01)
 * and the confirmation notification and CSV (US 01.05.02).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MakeReservationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var campsiteRepository: CampsiteRepository

    @Inject
    lateinit var reservationRepository: ReservationRepository

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val userId = "test-entrant"
    private val reservationId = "0"
    private val campsiteId = "camp-1"

    @Before
    fun setUp() {
        hiltRule.inject()

        // Signed in as an entrant, so the app opens on Browse
        (authRepository as FakeAuthRepository).setCurrentUserId(userId)
        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(User(userId = userId, userType = UserType.Entrant))
        (campsiteRepository as FakeCampsiteRepository).clear()
        (reservationRepository as FakeReservationRepository).clear()
        (notificationRepository as FakeNotificationRepository).clear()

        // The campsite the entrant won
        val campsite = Campsite(campsiteId = campsiteId, name = "Pocahontas", location = Location(address = "Jasper"))
        (campsiteRepository as FakeCampsiteRepository).addCampsite(campsite)

        // A won reservation that still needs to be confirmed
        val reservation = Reservation(
            reservationId = reservationId,
            reservationState = ReservationState.Pending,
            userId = userId,
            campsiteId = campsiteId
        )
        (reservationRepository as FakeReservationRepository).addReservation(reservation)
    }

    // Launches the app and opens the won reservation from the Booking tab
    private fun openMakeReservation(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/BROWSE").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Reservations").performClick()

        // Won reservations are under the Booking tab
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("tab_Booking").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("tab_Booking").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("reservation_$reservationId").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("reservation_$reservationId").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("confirm_reservation_button").fetchSemanticsNodes().isNotEmpty()
        }
        return scenario
    }

    @Test
    fun confirming_reservesAndOpensConfirmationScreen() {
        val scenario = openMakeReservation()

        composeRule.onNodeWithTag("confirm_reservation_button").performClick()

        // Confirmation screen appears
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("confirmation_screen").fetchSemanticsNodes().isNotEmpty()
        }

        // Reservation went from Pending to Reserved
        val state = runBlocking { reservationRepository.getReservation(reservationId)?.reservationState }
        assertEquals(ReservationState.Reserved, state)

        // CSV download button is there (US 01.05.02)
        composeRule.onNodeWithTag("download_csv_button").performScrollTo().assertIsDisplayed()

        scenario.close()
    }

    @Test
    fun confirming_sendsNotificationWithCampsite() {
        val scenario = openMakeReservation()

        composeRule.onNodeWithTag("confirm_reservation_button").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("confirmation_screen").fetchSemanticsNodes().isNotEmpty()
        }

        // One notification to the entrant, mentioning the campsite (US 01.05.02)
        val received = runBlocking { notificationRepository.getIncomingNotifications(userId) }
        assertEquals(1, received?.size)
        assertTrue(received!![0].notification.contains("Pocahontas"))

        scenario.close()
    }

    @Test
    fun confirmedReservation_showsUnderBooked() {
        val scenario = openMakeReservation()

        composeRule.onNodeWithTag("confirm_reservation_button").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Back to reservations").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Back to reservations").performScrollTo().performClick()

        // Reservations opens on Booked, where Reserved bookings are listed
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("reservation_$reservationId").fetchSemanticsNodes().isNotEmpty()
        }

        scenario.close()
    }
}