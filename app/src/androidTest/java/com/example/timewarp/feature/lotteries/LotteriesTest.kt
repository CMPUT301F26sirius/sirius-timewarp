package com.example.timewarp.feature.lotteries

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
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
import javax.inject.Inject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ENTRANT_ID = "lotteries-entrant"
private const val CAMPSITE_ID = "shared-campsite"
private const val FIRST_LOTTERY_ID = "first-lottery"
private const val SECOND_LOTTERY_ID = "second-lottery"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LotteriesTest {
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

    @Before
    fun setUp() {
        hiltRule.inject()

        (authRepository as FakeAuthRepository).setCurrentUserId(ENTRANT_ID)
        (userRepository as FakeUserRepository).apply {
            clear()
            addUser(User(userId = ENTRANT_ID, userType = UserType.Entrant))
        }
        (campsiteRepository as FakeCampsiteRepository).apply {
            clear()
            addCampsite(
                Campsite(
                    campsiteId = CAMPSITE_ID,
                    name = "Shared Campsite",
                    location = Location(address = "Test address"),
                    occupancy = Occupancy(maxGuests = 4),
                )
            )
        }
        (lotteryRepository as FakeLotteryRepository).apply {
            clear()
            addLottery(
                Lottery(
                    lotteryId = FIRST_LOTTERY_ID,
                    winnerCount = 4,
                    campsiteIds = listOf(CAMPSITE_ID),
                )
            )
            addLottery(
                Lottery(
                    lotteryId = SECOND_LOTTERY_ID,
                    winnerCount = 7,
                    campsiteIds = listOf(CAMPSITE_ID),
                )
            )
        }
        (reservationRepository as FakeReservationRepository).apply {
            clear()
            addReservation(
                Reservation(
                    reservationId = "pending-without-campsite",
                    reservationState = ReservationState.Pending,
                    lotteryId = FIRST_LOTTERY_ID,
                    userId = ENTRANT_ID,
                )
            )
            addReservation(
                Reservation(
                    reservationId = "pending-with-selected-campsite",
                    reservationState = ReservationState.Pending,
                    lotteryId = SECOND_LOTTERY_ID,
                    campsiteId = CAMPSITE_ID,
                    userId = ENTRANT_ID,
                )
            )
            addReservation(
                Reservation(
                    reservationId = "first-waitlisted",
                    reservationState = ReservationState.Waitlisted,
                    lotteryId = FIRST_LOTTERY_ID,
                    campsiteId = CAMPSITE_ID,
                    userId = ENTRANT_ID,
                )
            )
            addReservation(
                Reservation(
                    reservationId = "second-waitlisted-one",
                    reservationState = ReservationState.Waitlisted,
                    lotteryId = SECOND_LOTTERY_ID,
                    campsiteId = CAMPSITE_ID,
                    userId = ENTRANT_ID,
                )
            )
            addReservation(
                Reservation(
                    reservationId = "second-waitlisted-two",
                    reservationState = ReservationState.Waitlisted,
                    lotteryId = SECOND_LOTTERY_ID,
                    campsiteId = CAMPSITE_ID,
                    userId = "another-entrant",
                )
            )
        }
    }

    private fun openLotteries() {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Lotteries").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Lotteries").performClick()
    }

    private fun cardBadgeCount(badge: String): Int =
        composeRule.onAllNodesWithText(badge).fetchSemanticsNodes().size

    @Test
    fun entriesAndWaitlistCountsUseLotteryId() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openLotteries()

        composeRule.onNodeWithText("4 Winners - 1 People Waiting")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText("7 Winners - 2 People Waiting")
            .performScrollTo()
            .assertIsDisplayed()

        scenario.close()
    }

    @Test
    fun enteredTabShowsPendingEntries() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openLotteries()
        composeRule.waitUntil(5_000) { cardBadgeCount("ENTERED") == 2 }

        assertEquals(2, cardBadgeCount("ENTERED"))
        assertEquals(0, cardBadgeCount("BOOKING"))
        scenario.close()
    }

    @Test
    fun bookingTabShowsWaitlistedEntries() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openLotteries()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Booking").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Booking").performClick()
        composeRule.waitUntil(5_000) { cardBadgeCount("BOOKING") == 2 }

        assertEquals(2, cardBadgeCount("BOOKING"))
        assertEquals(0, cardBadgeCount("ENTERED"))
        scenario.close()
    }
}
