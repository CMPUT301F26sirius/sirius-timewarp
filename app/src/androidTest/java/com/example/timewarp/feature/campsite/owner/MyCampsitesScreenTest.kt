package com.example.timewarp.feature.campsite.owner

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
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
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
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


/** Instrumented tests for the owner's My Sites tab (US 02.03.01). */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MyCampsitesScreenTest {
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
    lateinit var lotteryRepository: LotteryRepository

    private val ownerId = "test-owner"

    @Before
    fun setUp() {
        hiltRule.inject()

        // Signed in as a campsite owner, so the My Sites tab is shown
        (authRepository as FakeAuthRepository).setCurrentUserId(ownerId)
        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(User(userId = ownerId, userType = UserType.CampsiteOwner))
        (campsiteRepository as FakeCampsiteRepository).clear()
        (lotteryRepository as FakeLotteryRepository).clear()
    }

    // Adds a campsite owned by [owner]
    private fun addCampsite(id: String, owner: String) {
        val campsite = Campsite(
            campsiteId = id,
            ownerUserId = owner,
            name = "Camp $id",
            location = Location(address = "Jasper, Alberta")
        )
        (campsiteRepository as FakeCampsiteRepository).addCampsite(campsite)
    }

    // Launches the app and opens the My Sites tab
    private fun openMyCampsites(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/BROWSE").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("My Sites").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/CAMPSITES/MINE").fetchSemanticsNodes().isNotEmpty()
        }
        return scenario
    }

    @Test
    fun ownCampsites_areShown_andOthersAreHidden() {
        addCampsite("mine", ownerId)
        addCampsite("theirs", "someone-else")
        val scenario = openMyCampsites()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("my_campsite_mine").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("my_campsite_mine").assertIsDisplayed()
        composeRule.onNodeWithTag("my_campsite_theirs").assertDoesNotExist()

        scenario.close()
    }

    @Test
    fun campsiteWithUpcomingLottery_showsLotteryLabel() {
        addCampsite("mine", ownerId)
        // Registration ends far in the future, so it counts as upcoming
        val lottery = Lottery(
            lotteryId = "l1",
            registrationPeriod = Period(start = 0, end = 100_000),
            campsiteIds = listOf("mine")
        )
        runBlocking { lotteryRepository.createLottery(lottery) }
        val scenario = openMyCampsites()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Lottery open or upcoming").fetchSemanticsNodes().isNotEmpty()
        }

        scenario.close()
    }

    @Test
    fun noCampsites_showsEmptyMessage() {
        val scenario = openMyCampsites()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("no_my_campsites").fetchSemanticsNodes().isNotEmpty()
        }

        scenario.close()
    }

    @Test
    fun tappingCampsite_opensCampsiteDetails() {
        addCampsite("mine", ownerId)
        val scenario = openMyCampsites()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("my_campsite_mine").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Camp mine").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/CAMPSITE/DETAILS").fetchSemanticsNodes().isNotEmpty()
        }

        scenario.close()
    }
}