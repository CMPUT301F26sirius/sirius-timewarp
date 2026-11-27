package com.example.timewarp.feature.reservation.details

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
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
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeNotificationRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.navigation.Screen
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
 * Instrumented tests for the role-based buttons on Manage reservation:
 * Decline for the entrant (US 01.04.03) and Reject for the campsite owner or an admin (US 02.08.01).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ReservationActionsTest {
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

    @Inject
    lateinit var lotteryRepository: LotteryRepository

    private val entrantId = "actions-entrant"
    private val ownerId = "actions-owner"
    private val adminId = "actions-admin"
    private val strangerId = "actions-stranger"
    private val campsiteId = "actions-campsite"
    private val lotteryId = "actions-lottery"
    private val reservationId = "actions-reservation"

    @Before
    fun setUp() {
        hiltRule.inject()

        (reservationRepository as FakeReservationRepository).clear()
        (notificationRepository as FakeNotificationRepository).clear()

        // One user of each kind
        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(User(userId = entrantId, userType = UserType.Entrant))
        (userRepository as FakeUserRepository).addUser(User(userId = ownerId, userType = UserType.CampsiteOwner))
        (userRepository as FakeUserRepository).addUser(User(userId = adminId, userType = UserType.Admin))
        (userRepository as FakeUserRepository).addUser(User(userId = strangerId, userType = UserType.Entrant))

        // The owner's campsite
        (campsiteRepository as FakeCampsiteRepository).clear()
        (campsiteRepository as FakeCampsiteRepository).addCampsite(
            Campsite(
                campsiteId = campsiteId,
                ownerUserId = ownerId,
                name = "Pocahontas",
                location = Location(address = "Jasper")
            )
        )

        // A lottery for that campsite, so won reservations without a campsite still belong to the owner
        (lotteryRepository as FakeLotteryRepository).clear()
        (lotteryRepository as FakeLotteryRepository).addLottery(
            Lottery(lotteryId = lotteryId, campsiteIds = listOf(campsiteId))
        )
    }

    // Signs in as one of the users added in setUp
    private fun signInAs(userId: String) {
        (authRepository as FakeAuthRepository).setCurrentUserId(userId)
    }

    // A won reservation only knows its lottery; the campsite is chosen when booking
    private fun addWonReservation() {
        val reservation = Reservation(
            reservationId = reservationId,
            reservationState = ReservationState.Pending,
            lotteryId = lotteryId,
            userId = entrantId
        )
        (reservationRepository as FakeReservationRepository).addReservation(reservation)
    }

    // A booked reservation at the owner's campsite
    private fun addBookedReservation() {
        val reservation = Reservation(
            reservationId = reservationId,
            reservationState = ReservationState.Reserved,
            lotteryId = lotteryId,
            userId = entrantId,
            campsiteId = campsiteId
        )
        (reservationRepository as FakeReservationRepository).addReservation(reservation)
    }

    // Opens Manage reservation from its deep link and waits until it has loaded
    private fun openReservationDetails(): ActivityScenario<MainActivity> {
        val link = "pitch://${Screen.ReservationDetails.createRoute(reservationId)}"
        val scenario = ActivityScenario.launch<MainActivity>(Intent(Intent.ACTION_VIEW, Uri.parse(link)))

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Reservation #").fetchSemanticsNodes().isNotEmpty()
        }
        return scenario
    }

    // Reads the test reservation's current state from the fake repository
    private fun savedState(): ReservationState? {
        return runBlocking { reservationRepository.getReservation(reservationId)?.reservationState }
    }

    @Test
    fun entrant_wonReservation_seesMakeAndDecline_notReject() {
        addWonReservation()
        signInAs(entrantId)
        val scenario = openReservationDetails()

        composeRule.onNodeWithTag("make_reservation_button").assertExists()
        composeRule.onNodeWithTag("decline_reservation_button").assertExists()
        composeRule.onNodeWithTag("reject_reservation_button").assertDoesNotExist()

        scenario.close()
    }

    @Test
    fun entrant_confirmsDecline_reservationIsDeclined() {
        addWonReservation()
        signInAs(entrantId)
        val scenario = openReservationDetails()

        composeRule.onNodeWithTag("decline_reservation_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("confirm_decline_button").performClick()

        // The state changes through the repository, where the auto-draw hooks in
        composeRule.waitUntil(5_000) {
            savedState() == ReservationState.Declined
        }

        scenario.close()
    }

    @Test
    fun owner_wonReservationInTheirLottery_seesRejectOnly() {
        addWonReservation()
        signInAs(ownerId)
        val scenario = openReservationDetails()

        // Owner can reject, but the entrant's own actions are hidden
        composeRule.onNodeWithTag("reject_reservation_button").assertExists()
        composeRule.onNodeWithTag("decline_reservation_button").assertDoesNotExist()
        composeRule.onNodeWithTag("make_reservation_button").assertDoesNotExist()

        scenario.close()
    }

    @Test
    fun owner_confirmsReject_reservationIsRejected_andEntrantNotified() {
        addBookedReservation()
        signInAs(ownerId)
        val scenario = openReservationDetails()

        // Owner can't cancel someone else's booking, only reject it
        composeRule.onNodeWithTag("cancel_booking_button").assertDoesNotExist()

        composeRule.onNodeWithTag("reject_reservation_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("confirm_reject_button").performClick()

        composeRule.waitUntil(5_000) {
            savedState() == ReservationState.Rejected
        }

        // One notification to the entrant, naming the campsite
        val received = runBlocking { notificationRepository.getIncomingNotifications(entrantId) }
        assertEquals(1, received?.size)
        assertTrue(received!![0].notification.contains("Pocahontas"))

        scenario.close()
    }

    @Test
    fun admin_canRejectAnyReservation() {
        addWonReservation()
        signInAs(adminId)
        val scenario = openReservationDetails()

        // Admins can do what owners can (US 03.11.01)
        composeRule.onNodeWithTag("reject_reservation_button").assertExists()

        scenario.close()
    }

    @Test
    fun stranger_seesNoActions() {
        addBookedReservation()
        signInAs(strangerId)
        val scenario = openReservationDetails()

        composeRule.onNodeWithText("This reservation can't be changed.").assertIsDisplayed()
        composeRule.onNodeWithTag("cancel_booking_button").assertDoesNotExist()
        composeRule.onNodeWithTag("reject_reservation_button").assertDoesNotExist()
        composeRule.onNodeWithTag("decline_reservation_button").assertDoesNotExist()

        scenario.close()
    }
}