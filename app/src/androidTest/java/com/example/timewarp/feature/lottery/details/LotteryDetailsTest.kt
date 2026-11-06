package com.example.timewarp.feature.lottery.details

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
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ENTRANT_ID = "fake-entrant"
private const val CAMPSITE_ID = "test-campsite"
private const val LOTTERY_ID = "test-lottery"

/*
* Instrumented tests for the lottery details screen that covers user stories
* 01.01.06, 01.02.01 and 01.02.02.
* The test runs against fake repositories. It covers opening lottery details,
* joining an active lottery, joining a waitlist, and leaving a waitlist.
*/
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LotteryDetailsTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var campsiteRepository: CampsiteRepository

    @Inject
    lateinit var lotteryRepository: LotteryRepository

    @Inject
    lateinit var reservationRepository: ReservationRepository

    // Today as a day number (days since 1970), the same unit Period uses
    private val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()).toInt()

    /*
    * This function logs a test entrant in, seeds one campsite and one open lottery,
    * and clears all reservations before each test
    */
    @Before
    fun setUp() {
        hiltRule.inject()

        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(User(userId = ENTRANT_ID, userType = UserType.Entrant))
        (authRepository as FakeAuthRepository).setCurrentUserId(ENTRANT_ID)

        (campsiteRepository as FakeCampsiteRepository).clear()
        (campsiteRepository as FakeCampsiteRepository).addCampsite(
            Campsite(
                campsiteId = CAMPSITE_ID,
                name = "Test Campsite",
                location = Location(address = "Test address"),
                occupancy = Occupancy(maxGuests = 4)
            )
        )

        (lotteryRepository as FakeLotteryRepository).clear()
        (lotteryRepository as FakeLotteryRepository).addLottery(
            Lottery(
                lotteryId = LOTTERY_ID,
                registrationPeriod = Period(start = 0, end = Int.MAX_VALUE),
                campsiteIds = listOf(CAMPSITE_ID)
            )
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
    * This function waits for 5 seconds for the tag to disappear from the screen
    */
    private fun waitForTagGone(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()
    }

    /*
    * This function opens the lottery's details from the campsite screen
    */
    private fun openLotteryDetails() {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Test Campsite").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Test Campsite").performClick()
        waitForTag("lottery_card_$LOTTERY_ID")
        composeRule.onNodeWithTag("lottery_card_$LOTTERY_ID")
            .performScrollTo()
            .performClick()
    }

    /*
    * This function replaces the lottery with one whose registration is over
    * but whose booking period is still going
    */
    private fun useClosedRegistrationLottery(capacity: Int = 100) {
        val lotteries = lotteryRepository as FakeLotteryRepository
        lotteries.clear()
        lotteries.addLottery(
            Lottery(
                lotteryId = LOTTERY_ID,
                registrationPeriod = Period(start = today - 10, end = today - 5),
                bookingPeriod = Period(start = today - 1, end = today + 10),
                waitlistCapacity = capacity,
                campsiteIds = listOf(CAMPSITE_ID)
            )
        )
    }

    /*
    * This function adds a waitlisted reservation at the test campsite
    */
    private fun addWaitlisted(id: String, userId: String) {
        (reservationRepository as FakeReservationRepository).addReservation(
            Reservation(
                reservationId = id,
                reservationState = ReservationState.Waitlisted,
                campsiteId = CAMPSITE_ID,
                userId = userId
            )
        )
    }

    /*
    * This test opens the lottery details from a campsite
    */
    @Test
    fun entrant_canOpenLotteryDetailsFromCampsite() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openLotteryDetails()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/LOTTERY/DETAILS")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("PITCH/LOTTERY/DETAILS").assertExists()
        scenario.close()
    }

    /*
    * US 01.01.06: This shows joining an active lottery during registration
    */
    @Test
    fun joinLottery_duringRegistration_010106() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openLotteryDetails()
        waitForTag("lottery_join")
        composeRule.onNodeWithTag("lottery_join").performClick()
        waitForTag("lottery_leave")
        composeRule.onNodeWithTag("lottery_leave").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.02.02: This shows a waitlisted user the leave waitlist button
    */
    @Test
    fun leaveWaitlist_showsLeaveButton_010202() {
        addWaitlisted("mine", ENTRANT_ID)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openLotteryDetails()
        waitForTag("lottery_leave")
        composeRule.onNodeWithTag("lottery_leave").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.02.02: This shows leaving the waitlist cancels the user's reservation
    */
    @Test
    fun leaveWaitlist_cancelsReservation_010202() {
        addWaitlisted("mine", ENTRANT_ID)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openLotteryDetails()
        waitForTag("lottery_leave")
        composeRule.onNodeWithTag("lottery_leave").performClick()
        waitForTagGone("lottery_leave")
        val reservation = runBlocking { reservationRepository.getReservation("mine") }
        assertEquals(ReservationState.Canceled, reservation?.reservationState)
        scenario.close()
    }
}