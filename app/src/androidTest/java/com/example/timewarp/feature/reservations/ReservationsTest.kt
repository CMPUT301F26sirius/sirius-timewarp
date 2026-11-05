package com.example.timewarp.feature.reservations

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ENTRANT_ID = "fake-entrant"

/**
 * Instrumented tests for reservation screen
 */

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ReservationsTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var fakeAuth: FakeAuthRepository

    @Inject
    lateinit var fakeUsers: FakeUserRepository

    @Inject
    lateinit var fakeReservations: FakeReservationRepository

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        hiltRule.inject()

        // Every test starts with no reservations and a signed-in entrant.
        fakeReservations.clear()
        fakeUsers.clear()
        fakeUsers.addUser(User(userId = ENTRANT_ID, userType = UserType.Entrant))
        fakeAuth.setCurrentUserId(ENTRANT_ID)
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

    @Test
    fun reservations_showsAllEntrantTabs() {
        openReservations()

        composeRule.onNodeWithText("Booked").assertIsDisplayed()
        composeRule.onNodeWithText("Booking").assertIsDisplayed()
        composeRule.onNodeWithText("Past").assertIsDisplayed()
        composeRule.onNodeWithText("Cancelled").assertIsDisplayed()
    }

    @Test
    fun bookingTab_showsOnlySignedInUsersReservations() {
        fakeReservations.addReservation(reservation(id = "mine-1", userId = ENTRANT_ID))
        fakeReservations.addReservation(reservation(id = "mine-2", userId = ENTRANT_ID))
        fakeReservations.addReservation(reservation(id = "someone-else", userId = "another-user"))
        openReservations()

        waitForText("There is still a chance!")

        val cards = composeRule.onAllNodesWithText("Make your reservation now!").fetchSemanticsNodes()
        assertEquals(2, cards.size)
    }

    @Test
    fun noReservations_showsEmptyView_andGoesToBrowse() {
        openReservations()

        waitForText("You haven't booked yet!")
        composeRule.onNodeWithText("Go To Browse?").assertIsDisplayed()

        composeRule.onNodeWithText("Go To Browse?").performClick()

        waitForText("A good day starts outside.")
        composeRule.onNodeWithText("A good day starts outside.").assertIsDisplayed()
    }

    @Test
    fun switchingTabs_changesWhatIsShown() {
        fakeReservations.addReservation(reservation(id = "mine-1", userId = ENTRANT_ID))
        openReservations()

        waitForText("There is still a chance!")

        composeRule.onNodeWithText("Booked").performClick()
        waitForText("Nothing booked yet!")
        composeRule.onNodeWithText("Go To Browse?").assertIsDisplayed()

        composeRule.onNodeWithText("Booking").performClick()
        waitForText("There is still a chance!")
        composeRule.onNodeWithText("Make your reservation now!").assertIsDisplayed()
    }

    @Test
    fun reservedReservation_showsUnderBookedTab() {
        fakeReservations.addReservation(
            reservation(id = "mine-1", userId = ENTRANT_ID, state = ReservationState.Reserved)
        )
        openReservations()

        composeRule.onNodeWithText("Booked").performClick()

        waitForText("Get ready to go!")
        composeRule.onNodeWithText("Tap to manage your reservation").assertIsDisplayed()
    }

    @Test
    fun tappingWaitlistedCard_opensManageReservation() {
        fakeReservations.addReservation(
            reservation(id = "mine-1", userId = ENTRANT_ID, state = ReservationState.Waitlisted)
        )
        openReservations()

        waitForText("Make your reservation now!")
        composeRule.onNodeWithText("Make your reservation now!").performClick()

        waitForText("Manage reservation")
        composeRule.onNodeWithText("Manage reservation").assertIsDisplayed()
    }

    @Test
    fun tappingPendingCard_opensMakeReservation() {
        fakeReservations.addReservation(
            reservation(id = "mine-1", userId = ENTRANT_ID, state = ReservationState.Pending)
        )
        openReservations()

        waitForText("Make your reservation now!")
        composeRule.onNodeWithText("Make your reservation now!").performClick()

        // Replace with the title your make-reservation screen shows (its Screen.title).
        waitForText("Make a reservation")
        composeRule.onNodeWithText("Make a reservation").assertIsDisplayed()
    }

    private fun openReservations() {
        scenario = ActivityScenario.launch(MainActivity::class.java)

        waitForText("Reservations")
        composeRule.onNodeWithText("Reservations").performClick()

        waitForText("PITCH/RESERVATIONS")
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun reservation(
        id: String,
        userId: String,
        state: ReservationState = ReservationState.Waitlisted,
    ) = Reservation(
        reservationId = id,
        reservationState = state,
        timestamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toInt(),
        campsiteId = "campsite-$id",
        userId = userId,
    )
}