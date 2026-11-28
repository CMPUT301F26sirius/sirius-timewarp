package com.example.timewarp.feature.lottery.edit

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performCustomAccessibilityActionWithLabel
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Lottery
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
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.isNotEmpty
import kotlin.collections.orEmpty
import kotlin.collections.single

/**
 * Instrumented tests, for editing a lottery as a campsite owner
 *
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EditLotteryTest {

    @get:
    Rule(order = 0) val hiltRule = HiltAndroidRule(this)

    @get:
    Rule(order = 1) val composeRule = createEmptyComposeRule()

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

    private val ownerId = "lottery-owner"
    private val editorId = "lottery-editor"
    private val lotteryId = "lottery-1"

    private val today = TimeUnit.MILLISECONDS.toDays((System.currentTimeMillis())).toInt()

    private  lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        hiltRule.inject()
        (authRepository as FakeAuthRepository).setCurrentUserId(ownerId)
        (userRepository as FakeUserRepository).addUser(
            User(userId = ownerId, userType = UserType.CampsiteOwner)
        )
        (userRepository as FakeUserRepository).addUser(
            User(userId = editorId, userType = UserType.CampsiteOwner)
        )
        (lotteryRepository as FakeLotteryRepository).clear()
        (reservationRepository as FakeReservationRepository).clear()
        val campRepository = campsiteRepository as FakeCampsiteRepository
        val lottRepository = lotteryRepository as FakeLotteryRepository
        campRepository.clear()
        campRepository.addCampsite(Campsite("1", ownerId,"Crimson Lake"))
        campRepository.addCampsite(Campsite("2", ownerId,"Emerald Lake"))
        campRepository.addCampsite(Campsite("3", "another-owner-id","Diamond Lake"))
        campRepository.addCampsite(Campsite("4", editorId,"Pine Ridge"))
        lottRepository.addLottery(
            Lottery(
                lotteryId = lotteryId,
                editorIds = listOf(editorId),
                registrationPeriod = Period(start = today, end = today + 2),
                bookingPeriod = Period(start = today + 4, end = today + 6),
                winnerCount = 3,
                waitlistCapacity = 25,
                campsiteIds = listOf("1"),
            )
        )
    }

    @Test
    fun campsiteList_showsLotteryOwnersAndEditorsCampsites() {
        openEditLottery()
        waitForText("Crimson Lake")
        composeRule.onNodeWithText("Crimson Lake").assertIsDisplayed()
        composeRule.onNodeWithText("Emerald Lake").assertIsDisplayed()
        composeRule.onNodeWithText("Pine Ridge").assertIsDisplayed()
        composeRule.onNodeWithText("Diamond Lake").assertDoesNotExist()
    }

    @Test
    fun lotteryEditor_canOpenEditAndSeeTheirCampsites() {
        (authRepository as FakeAuthRepository).setCurrentUserId(editorId)

        openEditLottery()

        composeRule.onNodeWithText("Pine Ridge").assertIsDisplayed()
        composeRule.onNodeWithText("Crimson Lake").assertIsDisplayed()
    }

    @Test
    fun campsiteList_notOwner_hideEdit() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Crimson Lake")
        composeRule.onNodeWithText("Crimson Lake").performClick()
        (authRepository as FakeAuthRepository).setCurrentUserId("different-owner")
        waitForTag("lottery_card_$lotteryId")
        composeRule.onNodeWithTag("lottery_card_$lotteryId").performScrollTo().performClick()
        waitForText("PITCH/LOTTERY/DETAILS")
        composeRule.onNodeWithText("Edit lottery").assertDoesNotExist()

    }

    @Test
    fun continueButton_isEnabledOnlyWhenCampsiteSelected() {
        openEditLottery()
        waitForText("Crimson Lake")
        composeRule.onNodeWithTag("lottery_details_button").assertIsEnabled()
        waitForButton("lottery_details_button", enabled = true)
        composeRule.onNodeWithText("Crimson Lake").assertIsOn()
        composeRule.onNodeWithText("Emerald Lake").assertIsOff()
        composeRule.onNodeWithText("Crimson Lake").performClick()
        waitForButton("lottery_details_button", enabled = false)
        composeRule.onNodeWithText("Crimson Lake").assertIsOff()
    }

    @Test
    fun editLottery_AllValidEntries_saveLotterytoRepository() {
        openEditLottery()
        fillCampsitesGoToDetails("Emerald Lake")

        pickDates("registration_picker", 10, 12)
        pickDates("booking_picker", 15, 18)
        numberField("winner_count_field").performTextReplacement("3")
        numberField("waitlist_count_field").performTextReplacement("25")

        waitForButton("lottery_create_button", enabled = true)
        composeRule.onNodeWithTag("lottery_create_button").performScrollTo().performClick()

        composeRule.waitUntil(5_000) {
            runBlocking { lotteryRepository.getLotteries() }.orEmpty().isNotEmpty()
        }
        val lottery = runBlocking { lotteryRepository.getLotteries() }.orEmpty().single()
        assertEquals(3, lottery.winnerCount)
        assertEquals(25, lottery.waitlistCapacity)
        assertEquals(setOf("1", "2"), lottery.campsiteIds.toSet())

        assertEquals(2, lottery.registrationPeriod.end - lottery.registrationPeriod.start)
        assertEquals(3, lottery.bookingPeriod.end - lottery.bookingPeriod.start)

        waitForText("Crimson Lake Lottery")
        composeRule.onNodeWithText("Complete Lottery Creation").assertDoesNotExist()
    }

    @Test
    fun editLottery_pastRegistrationPeriodDrawsConfiguredWinnerCount() {
        val reservations = reservationRepository as FakeReservationRepository
        val originalLottery = runBlocking { lotteryRepository.getLottery(lotteryId) }!!
        assertTrue(originalLottery.registrationPeriod.end >= today)
        repeat(5) { index ->
            reservations.addReservation(
                Reservation(
                    reservationId = "reservation-$index",
                    reservationState = ReservationState.Waitlisted,
                    lotteryId = lotteryId,
                    userId = "entrant-$index",
                )
            )
        }

        openEditLottery()
        fillCampsitesGoToDetails("Emerald Lake")
        pickPastRegistrationDates()
        waitForButton("lottery_create_button", enabled = true)
        composeRule.onNodeWithTag("lottery_create_button").performScrollTo().performClick()

        composeRule.waitUntil(5_000) {
            runBlocking {
                lotteryRepository.getLottery(lotteryId)?.registrationPeriod?.end
                    ?.let { it < today } == true
            }
        }
        val editedLottery = runBlocking { lotteryRepository.getLottery(lotteryId) }!!
        assertTrue(editedLottery.registrationPeriod.end < today)

        composeRule.waitUntil(5_000) {
            runBlocking {
                reservations.getReservations().orEmpty().count {
                    it.reservationState == ReservationState.Pending
                } == 3
            }
        }
        val drawnReservations = runBlocking { reservations.getReservations().orEmpty() }
        assertEquals(3, drawnReservations.count { it.reservationState == ReservationState.Pending })
        assertEquals(2, drawnReservations.count { it.reservationState == ReservationState.Waitlisted })
    }

    /*
    ##############################################################################
     */

    /*
   Open to Edit Lottery Screen
    */
    private fun openEditLottery() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Crimson Lake")
        composeRule.onNodeWithText("Crimson Lake").performClick()
        waitForTag("lottery_card_$lotteryId")
        composeRule.onNodeWithTag("lottery_card_$lotteryId").performScrollTo().performClick()
        waitForText("PITCH/LOTTERY/DETAILS")
        waitForText("Edit lottery")
        composeRule.onNodeWithText("Edit lottery").performScrollTo().performClick()
        waitForText("PITCH/LOTTERY/EDIT")
    }

    private fun fillCampsitesGoToDetails(vararg campsiteNames:String) {
        waitForText(campsiteNames.first())
        campsiteNames.forEach { composeRule.onNodeWithText(it).performClick() }
        waitForButton("lottery_details_button", enabled = true)
        composeRule.onNodeWithTag("lottery_details_button").performClick()
        waitForText("Complete Lottery Creation")
    }

    /*
    Finds field with given tag
     */
    private fun numberField(tag: String) =
        composeRule.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag(tag)))

    /*
    Check for value in field with given tag
     */
    private fun assertFieldValue(tag: String, value: String) {
        composeRule
            .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag(tag)) and hasText(value))
            .assertExists()
    }

    /*
    Find button to open date picker with given tag
     */
    private fun dateButton(tag: String) =
        composeRule.onNode(hasClickAction() and hasAnyAncestor(hasTestTag(tag)))

    /*
    Clicks a provided day (of a fixed month)
     */
    private fun clickDay(day: Int) {
        composeRule
            .onAllNodes(hasText(" $day,", substring = true) and hasClickAction())
            .onFirst()
            .performClick()
    }

    /*
    Picks 2 given dates
     */
    private fun pickDates(tag: String, startDay: Int, endDay: Int) {
        dateButton(tag).performClick()
        waitForText("Ok")
        clickDay(startDay)
        clickDay(endDay)
        composeRule.onNode(hasText("Ok") and isEnabled()).performClick()
        composeRule.waitForIdle()
    }

    @OptIn(ExperimentalTestApi::class)
    private fun pickPastRegistrationDates() {
        dateButton("registration_picker").performClick()
        waitForText("Ok")
        composeRule.onNodeWithText(DayFormatter.monthYear(today))
            .performCustomAccessibilityActionWithLabel("Scroll to show the previous month")
        composeRule.waitForIdle()
        clickDay(10)
        clickDay(12)
        composeRule.onNode(hasText("Ok") and isEnabled()).performClick()
        composeRule.waitForIdle()
        dateButton("registration_picker")
            .assertTextContains("10,", substring = true)
            .assertTextContains("12,", substring = true)
    }

    /*
    Press Create Button
     */
    private fun tapCreate() {
        waitForButton("lottery_create_button", enabled = true)
    }

    /*
    Waits 5 seconds for Text
     */
    private fun waitForText(text: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    private fun waitForTag(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

    /*
    Waits 5 seconds foe enabled Button
     */
    private fun waitForButton(tag: String, enabled: Boolean) = composeRule.waitUntil(5_000) {
        val state = if (enabled) isEnabled() else isNotEnabled()
        composeRule.onAllNodes(hasTestTag(tag) and state).fetchSemanticsNodes().isNotEmpty()
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
    }

}