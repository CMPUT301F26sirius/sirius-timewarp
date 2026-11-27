package com.example.timewarp.feature.lottery.waitlist

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


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WaitlistTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var lotteryRepository: LotteryRepository

    @Inject
    lateinit var campsiteRepository: CampsiteRepository

    @Inject
    lateinit var reservationRepository: ReservationRepository

    private val ownerId = "lottery-owner"
    private val lotteryId = "lottery-1"
    private  lateinit var scenario: ActivityScenario<MainActivity>

    private val today = TimeUnit.MILLISECONDS.toDays((System.currentTimeMillis())).toInt()

    /*
    * This functions logs test owner in, creates one lottery they own, creates a waitlist entry in that lottery
     */
    @Before
    fun setUp() {
        hiltRule.inject()
        (authRepository as FakeAuthRepository).setCurrentUserId(ownerId)
        (userRepository as FakeUserRepository).addUser(
            User(userId = ownerId, userType = UserType.CampsiteOwner)
        )
        (lotteryRepository as FakeLotteryRepository).clear()
        val campRepository = campsiteRepository as FakeCampsiteRepository
        val lottRepository = lotteryRepository as FakeLotteryRepository
        val resRepository = reservationRepository as FakeReservationRepository
        campRepository.clear()
        lottRepository.clear()
        resRepository.clear()
        campRepository.addCampsite(Campsite("1", ownerId,"Crimson Lake"))
        campRepository.addCampsite(Campsite("2", ownerId,"Emerald Lake"))
        campRepository.addCampsite(Campsite("3", "another-owner-id","Diamond Lake"))
        lottRepository.addLottery(
            Lottery(
                lotteryId = lotteryId,
                registrationPeriod = Period(start = today, end = today + 2),
                bookingPeriod = Period(start = today + 4, end = today + 6),
                winnerCount = 3,
                waitlistCapacity = 25,
                campsiteIds = listOf("1"),
            )
        )
        resRepository.addReservation(
            Reservation(
                reservationId = "resId",
                reservationState = ReservationState.Waitlisted,
                lotteryId = lotteryId,
                userId = "userId"
            )
        )
    }


    @Test
    fun lotteryExists_butNoReservations_showEmpty() {
        (reservationRepository as FakeReservationRepository).clear()
        openWaitlist()
        waitForText("Empty Waitlist")
    }

    @Test
    fun lotteryExists_butNoWaitlistedReservations_showEmpty() {
        (reservationRepository as FakeReservationRepository).clear()
        (reservationRepository as FakeReservationRepository).addReservation(
            Reservation(
                reservationId = "resId",
                reservationState = ReservationState.Pending,
                lotteryId = lotteryId,
                userId = "userId"
            )
        )
        openWaitlist()
        waitForText("Empty Waitlist")
    }

    @Test
    fun lotteryExists_hasWaitlist_showList() {
        openWaitlist()
        waitForText("User ID: userId")
    }

    @Test
    fun clickCard_openReservationDetails() {
        openWaitlist()
        waitForTag("waitlist_entry_resId")
        composeRule.onNodeWithTag("waitlist_entry_resId").performClick()
        waitForText("PITCH/RESERVATION/DETAILS")
        waitForText("resId")
    }

    private fun openWaitlist() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Crimson Lake")
        composeRule.onNodeWithText("Crimson Lake").performClick()
        waitForTag("lottery_card_$lotteryId")
        composeRule.onNodeWithTag("lottery_card_$lotteryId").performScrollTo().performClick()
        waitForText("PITCH/LOTTERY/DETAILS")
        waitForText("View Waitlist")
        composeRule.onNodeWithText("View Waitlist").performScrollTo().performClick()
        waitForText("PITCH/LOTTERY/WAITLIST")
    }

    private fun waitForText(text: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    private fun waitForTag(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

}