package com.example.timewarp.feature.reservation.make

import androidx.compose.ui.test.assertIsNotEnabled

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
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
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.domain.model.Lottery
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
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Availability
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
import androidx.compose.ui.test.assertIsEnabled

import com.example.timewarp.domain.model.Period

/**
 * Instrumented tests for confirming a won reservation (US 01.05.01),
 * the confirmation notification and CSV (US 01.05.02), and choosing a campsite and dates (US 01.04.01).
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

    @Inject
    lateinit var lotteryRepository: LotteryRepository

    private val userId = "test-entrant"
    private val reservationId = "0"
    private val campsiteId = "camp-1"

    private val otherCampsiteId = "camp-2"

    private val lotteryId = "lottery-1"

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
        (lotteryRepository as FakeLotteryRepository).clear()

        // The campsite the entrant won
        val campsite = Campsite(campsiteId = campsiteId, name = "Pocahontas", location = Location(address = "Jasper"))
        (campsiteRepository as FakeCampsiteRepository).addCampsite(campsite)



        val lotteryId = "test-lottery"
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        val fakeLotteryRepository = lotteryRepository as FakeLotteryRepository

        fakeLotteryRepository.clear()
        fakeLotteryRepository.addLottery(
            Lottery(
                lotteryId = lotteryId,
                bookingPeriod = Period(start = today - 1, end = today + 1),
                campsiteIds = listOf(campsiteId)
            )
        )

        // A won reservation that still needs to be confirmed
        val reservation = Reservation(
            reservationId = reservationId,
            reservationState = ReservationState.Pending,
            userId = userId,
            campsiteId = campsiteId,
            lotteryId = lotteryId,

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

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("tab_Pending").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("tab_Pending").performClick()

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

        val confirmButton = composeRule.onNodeWithTag("confirm_reservation_button")
        confirmButton.assertIsNotEnabled()

        composeRule.onNodeWithTag("campsite_rules_acknowledgment").performClick()

        confirmButton.assertIsEnabled()
        confirmButton.performClick()

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


        composeRule.onNodeWithTag("campsite_rules_acknowledgment").performClick()

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

        composeRule.onNodeWithTag("campsite_rules_acknowledgment").performClick()

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

    // Gives the won campsite open dates a month from now, so the screen asks for dates
    private fun openCampsiteDates() {
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        val campsite = Campsite(
            campsiteId = campsiteId,
            name = "Pocahontas",
            location = Location(address = "Jasper"),
            availability = Availability(
                startDate = DayFormatter.toDateString(today + 30),
                endDate = DayFormatter.toDateString(today + 40)
            )
        )
        // Same ID, so this replaces the campsite added in setUp
        (campsiteRepository as FakeCampsiteRepository).addCampsite(campsite)
    }

    @Test
    fun withOpenDates_confirmDisabledUntilDatesChosen() {
        openCampsiteDates()
        val scenario = openMakeReservation()

        composeRule.onNodeWithTag("choose_dates_button").assertIsDisplayed()
        composeRule.onNodeWithText("No dates chosen yet").assertIsDisplayed()

        composeRule.onNodeWithTag("campsite_rules_acknowledgment").performClick()

        composeRule.onNodeWithTag("confirm_reservation_button").assertIsNotEnabled()

        scenario.close()
    }

    @Test
    fun chooseDates_opensCalendar() {
        openCampsiteDates()
        val scenario = openMakeReservation()

        composeRule.onNodeWithTag("choose_dates_button").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Select your dates").fetchSemanticsNodes().isNotEmpty()
        }

        // Closing without picking keeps Confirm off
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("No dates chosen yet").assertIsDisplayed()

        scenario.close()
    }

    // Replaces the won reservation with one from a lottery with two campsites, the way join saves it now
    private fun openTwoCampsiteLottery() {
        val otherCampsite = Campsite(campsiteId = otherCampsiteId, name = "Wapiti", location = Location(address = "Jasper"))
        (campsiteRepository as FakeCampsiteRepository).addCampsite(otherCampsite)

        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        val lottery = Lottery(
            lotteryId = lotteryId,
            bookingPeriod = Period(start = today - 1, end = today + 1),
            campsiteIds = listOf(campsiteId, otherCampsiteId)
        )
        (lotteryRepository as FakeLotteryRepository).addLottery(lottery)

        // Join saves only the lotteryId; the campsite is chosen when booking
        val reservation = Reservation(
            reservationId = reservationId,
            reservationState = ReservationState.Pending,
            userId = userId,
            lotteryId = lotteryId
        )
        // Same ID, so this replaces the reservation added in setUp
        (reservationRepository as FakeReservationRepository).addReservation(reservation)
    }

    @Test
    fun twoCampsiteLottery_showsBothCampsites() {
        openTwoCampsiteLottery()
        val scenario = openMakeReservation()

        composeRule.onNodeWithTag("campsite_dropdown").performClick()

        composeRule.onNodeWithTag("campsite_option_$campsiteId").assertIsDisplayed()
        composeRule.onNodeWithTag("campsite_option_$otherCampsiteId").assertIsDisplayed()

        scenario.close()
    }

    @Test
    fun choosingOtherCampsite_confirmSavesIt() {
        openTwoCampsiteLottery()
        val scenario = openMakeReservation()

        // Pick the second campsite, then confirm
        // Open the menu, pick the second campsite, then confirm
        composeRule.onNodeWithTag("campsite_dropdown").performClick()
        composeRule.onNodeWithTag("campsite_option_$otherCampsiteId").performClick()
        composeRule.onNodeWithTag("campsite_rules_acknowledgment").performClick()
        composeRule.onNodeWithTag("confirm_reservation_button").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("confirmation_screen").fetchSemanticsNodes().isNotEmpty()
        }

        // Reserved at the campsite the winner chose
        val saved = runBlocking { reservationRepository.getReservation(reservationId) }
        assertEquals(ReservationState.Reserved, saved?.reservationState)
        assertEquals(otherCampsiteId, saved?.campsiteId)

        scenario.close()
    }

    @Test
    fun declining_setsDeclined_andReturnsToReservations() {
        val scenario = openMakeReservation()

        composeRule.onNodeWithTag("decline_reservation_button").performClick()
        composeRule.onNodeWithTag("confirm_decline_button").performClick()

        // The state changes through the repository, where the auto-draw hooks in
        composeRule.waitUntil(5_000) {
            runBlocking { reservationRepository.getReservation(reservationId)?.reservationState } ==
                    ReservationState.Declined
        }

        // Back on the Reservations screen it came from (US 01.04.03)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/RESERVATIONS").fetchSemanticsNodes().isNotEmpty()
        }

        scenario.close()
    }

    @Test
    fun keepIt_doesNotDecline() {
        val scenario = openMakeReservation()

        composeRule.onNodeWithTag("decline_reservation_button").performClick()
        composeRule.onNodeWithText("Keep it").performClick()

        // Still a won reservation, still on the screen
        val state = runBlocking { reservationRepository.getReservation(reservationId)?.reservationState }
        assertEquals(ReservationState.Pending, state)
        composeRule.onNodeWithTag("confirm_reservation_button").assertIsDisplayed()

        scenario.close()
    }
}