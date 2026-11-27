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
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeProfileRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Availability
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.ProfileRepository
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
    Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:
    Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var reservationRepository: ReservationRepository

    @Inject
    lateinit var lotteryRepository: LotteryRepository

    @Inject
    lateinit var campsiteRepository: CampsiteRepository

    @Inject
    lateinit var profileRepository: ProfileRepository

    /*
    * This function builds a test reservation with the given id, user, and state
    */
    private fun reservation(
        id: String,
        userId: String,
        state: ReservationState,
        lotteryId: String = "",
    ) =
        Reservation(
            reservationId = id,
            reservationState = state,
            lotteryId = lotteryId,
            campsiteId = "campsite",
            userId = userId
        )

    private fun addLottery(lotteryId: String, bookingStart: Int, bookingEnd: Int) {
        (lotteryRepository as FakeLotteryRepository).addLottery(
            Lottery(
                lotteryId = lotteryId,
                bookingPeriod = Period(start = bookingStart, end = bookingEnd),
            )
        )
    }

    private fun reservedReservation(id: String, endDay: Int) =
        Reservation(
            reservationId = id,
            reservationState = ReservationState.Reserved,
            campsiteId = "campsite",
            userId = "reservations-user",
            datesBooked = Availability(endDate = DayFormatter.toDateString(endDay)),
        )

    /*
   * This function signs in a campsite owner who owns "Owner Camp" and "Second Camp",
   * and adds a camp they don't own, plus a guest with a name
   */
    private fun signInOwner() {
        (userRepository as FakeUserRepository).addUser(
            User(userId = "owner-user", userType = UserType.CampsiteOwner)
        )
        (authRepository as FakeAuthRepository).setCurrentUserId("owner-user")

        val campsites = campsiteRepository as FakeCampsiteRepository
        campsites.addCampsite(
            Campsite(
                campsiteId = "owner-camp", ownerUserId = "owner-user", name = "Owner Camp"
            )
        )
        campsites.addCampsite(
            Campsite(
                campsiteId = "second-camp",
                ownerUserId = "owner-user",
                name = "Second Camp"
            )
        )
        campsites.addCampsite(
            Campsite(
                campsiteId = "other-camp", ownerUserId = "someone-else", name = "Other Camp"
            )
        )

        (profileRepository as FakeProfileRepository).addProfile(
            Profile(userId = "guest-user", firstName = "John", lastName = "Doe")
        )
    }

    /*
    * This function builds a guest's reservation at the given campsite with stay dates
    */
    private fun guestReservation(
        id: String,
        campsiteId: String,
        state: ReservationState,
        startDay: Int,
        endDay: Int,
        userId: String = "guest-user",
    ) =
        Reservation(
            reservationId = id,
            reservationState = state,
            campsiteId = campsiteId,
            userId = userId,
            datesBooked = Availability(
                startDate = DayFormatter.toDateString(startDay),
                endDate = DayFormatter.toDateString(endDay),
            ),
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
        (lotteryRepository as FakeLotteryRepository).clear()
        (campsiteRepository as FakeCampsiteRepository).clear()
        (profileRepository as FakeProfileRepository).clear()
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
        waitForTag("tab_Reserved")
    }

    /*
   * This function switches to "Reservations At My Camps"
   */
    private fun openAtMyCamps() {
        waitForTag("scope_${ReservationToDisplay.AtMyCamps.label}")
        composeRule.onNodeWithTag("scope_${ReservationToDisplay.AtMyCamps.label}").performClick()
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
    * US 01.06.01: This shows only pending winner reservations on the Booking tab
    */
    @Test
    fun bookingTab_showsPending() {
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(
            reservation(
                "waiting",
                "reservations-user",
                ReservationState.Waitlisted
            )
        )
        repository.addReservation(reservation("won", "reservations-user", ReservationState.Pending))
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        composeRule.onNodeWithTag("tab_${ReservationTab.Booking.label}").performClick()
        waitForTag("reservation_won")
        composeRule.onNodeWithTag("reservation_won").assertIsDisplayed()
        scenario.close()
    }

    // Shows the pending tab
    @Test
    fun bookingTab_showsWonEntries_notWaitlisted() {
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        addLottery("open-lottery", today, today)
        addLottery("ended-lottery", today - 5, today - 1)
        addLottery("future-lottery", today + 1, today + 5)

        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(
            reservation(
                "open-pending",
                "reservations-user",
                ReservationState.Pending,
                "open-lottery"
            )
        )
        repository.addReservation(
            reservation(
                "open-waitlisted",
                "reservations-user",
                ReservationState.Waitlisted,
                "open-lottery"
            )
        )
        repository.addReservation(
            reservation(
                "ended-pending",
                "reservations-user",
                ReservationState.Pending,
                "ended-lottery"
            )
        )
        repository.addReservation(
            reservation(
                "ended-waitlisted",
                "reservations-user",
                ReservationState.Waitlisted,
                "ended-lottery"
            )
        )
        repository.addReservation(
            reservation(
                "future-pending",
                "reservations-user",
                ReservationState.Pending,
                "future-lottery"
            )
        )

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        composeRule.onNodeWithTag("tab_${ReservationTab.Booking.label}").performClick()
        waitForTag("reservation_open-pending")

        // Every won entry shows, whatever its booking period
        composeRule.onNodeWithTag("reservation_open-pending").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_ended-pending").assertExists()
        composeRule.onNodeWithTag("reservation_future-pending").assertExists()

        // Waitlisted entries belong on the Lotteries screen, not here
        composeRule.onNodeWithTag("reservation_open-waitlisted").assertDoesNotExist()
        composeRule.onNodeWithTag("reservation_ended-waitlisted").assertDoesNotExist()
        scenario.close()
    }

    /*
    * US 01.06.01: This shows only the signed in user's reservations, not other users'
    */
    @Test
    fun showsOnlyOwnReservations() {
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(
            reservation(
                "mine",
                "reservations-user",
                ReservationState.Reserved
            )
        )
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
        composeRule.onNodeWithTag("tab_${ReservationTab.Cancelled.label}").performClick()
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
        repository.addReservation(
            reservation(
                "active",
                "reservations-user",
                ReservationState.Reserved
            )
        )
        repository.addReservation(
            reservation(
                "cancelled",
                "reservations-user",
                ReservationState.Canceled
            )
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        waitForTag("reservation_active")
        composeRule.onNodeWithTag("reservation_cancelled").assertDoesNotExist()
        scenario.close()
    }

    @Test
    fun reservedTripsAreGroupedByEndDateAcrossTabs() {
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(reservedReservation("past", today - 1))
        repository.addReservation(reservedReservation("ending-today", today))
        repository.addReservation(reservedReservation("upcoming", today + 1))
        repository.addReservation(
            reservation(
                "pending",
                "reservations-user",
                ReservationState.Pending
            )
        )

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        waitForTag("reservation_upcoming")
        composeRule.onNodeWithTag("reservation_upcoming").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_ending-today").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_past").assertDoesNotExist()
        composeRule.onNodeWithTag("reservation_pending").assertDoesNotExist()

        composeRule.onNodeWithTag("tab_Past").performClick()
        waitForTag("reservation_past")
        composeRule.onNodeWithTag("reservation_past").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_ending-today").assertDoesNotExist()
        composeRule.onNodeWithTag("reservation_upcoming").assertDoesNotExist()
        composeRule.onNodeWithTag("reservation_pending").assertDoesNotExist()
        scenario.close()
    }

    // Entrants only have their own reservations, so they see no scope chips
    @Test
    fun entrant_seesNoScopeChips_020701() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        composeRule.onNodeWithTag("scope_${ReservationToDisplay.AtMyCamps.label}")
            .assertDoesNotExist()
        scenario.close()
    }

    // Owners see both scope chips
    @Test
    fun owner_seesScopeChips_020701() {
        signInOwner()
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        waitForTag("scope_${ReservationToDisplay.AtMyCamps.label}")
        composeRule.onNodeWithTag("scope_${ReservationToDisplay.Mine.label}").assertIsDisplayed()
        composeRule.onNodeWithTag("scope_${ReservationToDisplay.AtMyCamps.label}")
            .assertIsDisplayed()
        scenario.close()
    }

    // At My Camps shows only reservations at campsites the owner owns
    @Test
    fun atMyCamps_showsOnlyOwnedCampsites_020701() {
        signInOwner()
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(
            guestReservation(
                "at-mine",
                "owner-camp",
                ReservationState.Reserved,
                today + 1,
                today + 3
            )
        )
        repository.addReservation(
            guestReservation(
                "elsewhere",
                "other-camp",
                ReservationState.Reserved,
                today + 1,
                today + 3
            )
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        openAtMyCamps()
        waitForTag("reservation_at-mine")
        composeRule.onNodeWithTag("reservation_at-mine").assertIsDisplayed()
        composeRule.onNodeWithTag("reservation_elsewhere").assertDoesNotExist()
        scenario.close()
    }

    /*
    * The owner can filter the list by campsite
    */
    @Test
    fun atMyCamps_filterByCampsite_020701() {
        signInOwner()
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(
            guestReservation(
                "first",
                "owner-camp",
                ReservationState.Reserved,
                today + 1,
                today + 3
            )
        )
        repository.addReservation(
            guestReservation(
                "second",
                "second-camp",
                ReservationState.Reserved,
                today + 1,
                today + 3
            )
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        openAtMyCamps()
        waitForTag("reservation_first")
        composeRule.onNodeWithTag("campsite_filter").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Second Camp").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Second Camp").performClick()
        waitForTag("reservation_second")
        composeRule.onNodeWithTag("reservation_first").assertDoesNotExist()
        scenario.close()
    }

    // A stay that has ended shows under Past, not Reserved
    @Test
    fun atMyCamps_endedStayOnPastTab_020702() {
        signInOwner()
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        (reservationRepository as FakeReservationRepository).addReservation(
            guestReservation("ended", "owner-camp", ReservationState.Reserved, today - 5, today - 1)
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        openAtMyCamps()
        composeRule.onNodeWithTag("reservation_ended").assertDoesNotExist()
        composeRule.onNodeWithTag("tab_${ReservationTab.Past.label}").performClick()
        waitForTag("reservation_ended")
        composeRule.onNodeWithTag("reservation_ended").assertIsDisplayed()
        scenario.close()
    }

   //Cancelled reservations at the owner's camps show on the Cancelled tab
    @Test
    fun atMyCamps_cancelledTab_020703() {
        signInOwner()
        val today = DayFormatter.fromMillis(System.currentTimeMillis())
        (reservationRepository as FakeReservationRepository).addReservation(
            guestReservation(
                "cancelled",
                "owner-camp",
                ReservationState.Canceled,
                today + 1,
                today + 3
            )
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        openAtMyCamps()
        composeRule.onNodeWithTag("tab_${ReservationTab.Cancelled.label}").performClick()
        waitForTag("reservation_cancelled")
        composeRule.onNodeWithTag("reservation_cancelled").assertIsDisplayed()
        scenario.close()
    }

    // With no cancelled reservations, the empty message is shown
    @Test
    fun atMyCamps_noCancelled_showsEmptyMessage_020703() {
        signInOwner()
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openReservations()
        openAtMyCamps()
        composeRule.onNodeWithTag("tab_${ReservationTab.Cancelled.label}").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(ReservationTab.Cancelled.emptyHeading)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(ReservationTab.Cancelled.emptyHeading).assertIsDisplayed()
        scenario.close()
    }
}