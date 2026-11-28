package com.example.timewarp.feature.campsite.booked

import android.content.Intent
import android.net.Uri
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
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeProfileRepository
import com.example.timewarp.data.repository.FakeRatingRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Availability
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.navigation.Screen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumented tests for the owner's View booked dates button and Booked dates screen (US 02.06.01).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BookedDatesTest {
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
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var ratingRepository: RatingRepository

    @Inject
    lateinit var lotteryRepository: LotteryRepository

    @Inject
    lateinit var reservationRepository: ReservationRepository

    private val ownerId = "booked-owner"
    private val entrantId = "booked-entrant"
    private val campsiteId = "booked-campsite"
    private val reservationId = "booked-reservation"
    private val campsiteLink = "pitch://${Screen.CampsiteDetails.createRoute(campsiteId)}"

    @Before
    fun setUp() {
        hiltRule.inject()

        (userRepository as FakeUserRepository).clear()
        (ratingRepository as FakeRatingRepository).clear()
        (lotteryRepository as FakeLotteryRepository).clear()
        (reservationRepository as FakeReservationRepository).clear()

        // The campsite page shows the owner's profile, so it needs one
        (profileRepository as FakeProfileRepository).clear()
        (profileRepository as FakeProfileRepository).addProfile(
            Profile(userId = ownerId, firstName = "Booked", lastName = "Owner")
        )

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
    }

    // Signs in as the given user with the given type
    private fun signInAs(userId: String, userType: UserType) {
        (authRepository as FakeAuthRepository).setCurrentUserId(userId)
        (userRepository as FakeUserRepository).addUser(User(userId = userId, userType = userType))
    }

    // Adds a Reserved stay at the campsite from checkIn to checkOut
    private fun addBookedStay(checkIn: Int, checkOut: Int) {
        val reservation = Reservation(
            reservationId = reservationId,
            reservationState = ReservationState.Reserved,
            userId = entrantId,
            campsiteId = campsiteId,
            datesBooked = Availability(
                startDate = DayFormatter.toDateString(checkIn),
                endDate = DayFormatter.toDateString(checkOut)
            )
        )
        (reservationRepository as FakeReservationRepository).addReservation(reservation)
    }

    // Opens the campsite page from its deep link and taps View booked dates
    private fun openBookedDates(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch<MainActivity>(
            Intent(Intent.ACTION_VIEW, Uri.parse(campsiteLink))
        )

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("view_booked_dates_button").fetchSemanticsNodes().isNotEmpty()
        }
        // The page scrolls, and the owner buttons can be below the screen
        composeRule.onNodeWithTag("view_booked_dates_button").performScrollTo().performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Current and upcoming bookings").fetchSemanticsNodes().isNotEmpty()
        }
        return scenario
    }

    @Test
    fun owner_seesBookedStay() {
        signInAs(ownerId, UserType.CampsiteOwner)

        // A stay 10 to 12 days from now, so it's always upcoming
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        addBookedStay(today + 10, today + 12)

        val scenario = openBookedDates()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("booked_stay_$reservationId").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("booked_stay_$reservationId").assertIsDisplayed()

        // The card shows the stay's dates and number of nights
        val datesText = "${DayFormatter.shortDate(today + 10)} – ${DayFormatter.shortDate(today + 12)}"
        composeRule.onNodeWithText(datesText).assertIsDisplayed()
        composeRule.onNodeWithText("2 nights").assertIsDisplayed()

        scenario.close()
    }

    @Test
    fun owner_withNoBookings_seesEmptyMessage() {
        signInAs(ownerId, UserType.CampsiteOwner)

        val scenario = openBookedDates()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("no_booked_dates").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("no_booked_dates").assertIsDisplayed()

        scenario.close()
    }

    @Test
    fun entrant_doesNotSeeViewBookedDatesButton() {
        signInAs(entrantId, UserType.Entrant)

        val scenario = ActivityScenario.launch<MainActivity>(
            Intent(Intent.ACTION_VIEW, Uri.parse(campsiteLink))
        )

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Pocahontas").fetchSemanticsNodes().isNotEmpty()
        }
        // Only the owner can see other people's bookings
        composeRule.onNodeWithTag("view_booked_dates_button").assertDoesNotExist()

        scenario.close()
    }
}