package com.example.timewarp.feature.lottery.edit

import android.icu.util.TimeUnit
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

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

    private val ownerId = "lottery-owner"
    private val lotteryId = "lottery-1"

    private  lateinit var scenario: ActivityScenario<MainActivity>

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
        campRepository.clear()
        campRepository.addCampsite(Campsite("1", ownerId,"Crimson Lake"))
        campRepository.addCampsite(Campsite("2", ownerId,"Emerald Lake"))
        campRepository.addCampsite(Campsite("3", "another-owner-id","Diamond Lake"))
        lottRepository.addLottery(
            Lottery(
                lotteryId = lotteryId,
                registrationPeriod = Period(start = 0, end = 1),
                bookingPeriod = Period(start = 2, end = 3),
                winnerCount = 3,
                waitlistCapacity = 25,
                campsiteIds = listOf("1"),
            )
        )
    }

    @Test
    fun campsiteList_showOnlyUsersOwnedCampsites() {
        openEditLottery()
        waitForText("Crimson Lake")
        composeRule.onNodeWithText("Crimson Lake").assertIsDisplayed()
        composeRule.onNodeWithText("Emerald Lake").assertIsDisplayed()
        composeRule.onNodeWithText("Diamond Lake").assertDoesNotExist()
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
        waitForText("lottery_card_\$lotteryId")
        composeRule.onNodeWithText("lottery_card_\$lotteryId").performClick()
        waitForText("PITCH/LOTTERY/DETAILS")
        waitForText("Edit lottery")
        composeRule.onNodeWithText("lottery_card_\$lotteryId").performClick()
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
        composeRule.onNodeWithText("Ok").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Ok").fetchSemanticsNodes().isEmpty()
        }
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