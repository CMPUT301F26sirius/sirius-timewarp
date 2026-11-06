// Parts of this code were made with the assistance of Claude AI
// While no functionally was implemented wholly by the AI
// I prompted it with questions about how to do certain things in Kotlin/Android
// Or assistance in debugging

package com.example.timewarp.feature.example

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
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
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.UserRepository
import com.google.firestore.v1.Value
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Instrumented tests, for creating a lottery as a campsite owner
 *
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateLotteryTest() {

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

    private  lateinit var scenario: ActivityScenario<MainActivity>

    /**
     * This function logs a campsite owner in
     * Creates 2 campsites they own
     * Creates 1 campsite which belongs to somebody else before each test
     */
    @Before
    fun setUp() {
        hiltRule.inject()
        (authRepository as FakeAuthRepository).setCurrentUserId(ownerId)
        (userRepository as FakeUserRepository).addUser(
            User(userId = ownerId, userType = UserType.CampsiteOwner)
        )
        (lotteryRepository as FakeLotteryRepository).clear()
        val repository = campsiteRepository as FakeCampsiteRepository
        repository.clear()
        repository.addCampsite(Campsite("1", ownerId,"Crimson Lake"))
        repository.addCampsite(Campsite("2", ownerId,"Emerald Lake"))
        repository.addCampsite(Campsite("3", "another-owner-id","Diamond Lake"))

    }

    @Test
    fun campsiteList_showOnlyUsersOwnedCampsites() {
        openCreateLottery()
        waitForText("Crimson Lake")
        composeRule.onNodeWithText("Crimson Lake").assertIsDisplayed()
        composeRule.onNodeWithText("Emerald Lake").assertIsDisplayed()
        composeRule.onNodeWithText("Diamond Lake").assertDoesNotExist()
    }

    @Test
    fun campsiteList_noSites_showEmpty() {
        (campsiteRepository as FakeCampsiteRepository).clear()
        openCreateLottery()
        waitForText("You don't own any campsites")
        composeRule.onNodeWithTag("lottery_details_button").assertIsNotEnabled()
    }

    @Test
    fun continueButton_isEnabledOnlyWhenCampsiteSelected() {
        openCreateLottery()
        waitForText("Crimson Lake")
        composeRule.onNodeWithTag("lottery_details_button").assertIsNotEnabled()
        composeRule.onNodeWithText("Crimson Lake").performClick()
        waitForButton("lottery_details_button", enabled = true)
        composeRule.onNodeWithText("Crimson Lake").assertIsOn()
        composeRule.onNodeWithText("Emerald Lake").assertIsOff()
        composeRule.onNodeWithText("Crimson Lake").performClick()
        waitForButton("lottery_details_button", enabled = false)
        composeRule.onNodeWithText("Crimson Lake").assertIsOff()
    }

    @Test
    fun continue_withSelectedCampsite_goToDetails() {
        openCreateLottery()
        fillCampsitesGoToDetails("Crimson Lake")
        composeRule.onNodeWithText("Complete Lottery Creation").assertIsDisplayed()
        composeRule.onNodeWithText("Crimson Lake").assertDoesNotExist()
    }

    @Test
    fun details_noEntry_cannotCreate() {
        openCreateLottery()
        fillCampsitesGoToDetails("Crimson Lake")
        assertFieldValue("winner_count_field", "1")
        assertFieldValue("waitlist_count_field", "100")
        composeRule.onAllNodesWithText("Select Dates").assertCountEquals(2)
        composeRule.onNodeWithTag("lottery_create_button").assertIsNotEnabled()
    }

    @Test
    fun createLottery_AllValidEntries_saveLotterytoRepository() {
        openCreateLottery()
        fillCampsitesGoToDetails("Crimson Lake", "Emerald Lake")

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

        waitForText("Cross your fingers!")
        composeRule.onNodeWithText("Complete Lottery Creation").assertDoesNotExist()
    }





    /*
    Open to Create Lottery Screen
     */
    private fun openCreateLottery() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Lotteries")
        composeRule.onNodeWithText("Lotteries").performClick()
        waitForText("Create lottery")
        composeRule.onNodeWithText("Create lottery").performClick()
        waitForText("PITCH/LOTTERY/CREATE")
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