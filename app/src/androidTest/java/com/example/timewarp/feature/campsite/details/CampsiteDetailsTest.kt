package com.example.timewarp.feature.campsite.details

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
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
import com.example.timewarp.domain.model.Occupancy
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CampsiteDetailsTest {

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

    private val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()).toInt()

    /*
    * This function logs a test entrant in and seeds a campsite before each test
    */
    @Before
    fun setUp() {
        hiltRule.inject()

        // Entrant
        (authRepository as FakeAuthRepository).setCurrentUserId("browse-user")
        (userRepository as FakeUserRepository).addUser(User(userId = "browse-user", userType = UserType.Entrant))

        // Campsite
        (campsiteRepository as FakeCampsiteRepository).clear()
        (lotteryRepository as FakeLotteryRepository).clear()
        (campsiteRepository as FakeCampsiteRepository).addCampsite(
            Campsite(
                campsiteId = "1",
                ownerUserId = "owner-user",
                name = "Crimson Lake",
                rating = 4.5,
                location = Location(address = "Rocky Mountain House, AB"),
                occupancy = Occupancy(maxGuests = 4)
            )
        )
    }

    /*
    * This function opens the details screen by clicking the seeded campsite on the
    * Browse screen
    */
    private fun openDetails() {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Crimson Lake").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Crimson Lake").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("details_name").fetchSemanticsNodes().isNotEmpty()
        }
    }

    /*
    * US 01.01.05: This shows the details screen and shows the name, address, map and rules
    * for the campsite
    */
    @Test
    fun details_showName_Address_Rules_010105(){
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openDetails()
        composeRule.onNodeWithTag("details_name").assertIsDisplayed()
        composeRule.onNodeWithTag("details_address").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.01.05: This shows that the details screen has a map and rules section
    * for the campsite
    */
    @Test
    fun details_showMap_Rules_010105() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openDetails()
        composeRule.onNodeWithTag("details_map").assertExists()
        composeRule.onNodeWithTag("details_rules").assertExists()
        scenario.close()
    }

    @Test
    fun details_showsLotteryInRegistrationPeriod() {
        (lotteryRepository as FakeLotteryRepository).addLottery(
            Lottery(
                lotteryId = "active-lottery",
                registrationPeriod = Period(start = today - 1, end = today + 6000),
                winnerCount = 3,
                waitlistCapacity = 25,
                campsiteIds = listOf("1")
            )
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        openDetails()

        composeRule.onNodeWithText("Lottery").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Winners: 3").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Waitlist spaces: 25").performScrollTo().assertIsDisplayed()

        scenario.close()
    }

    @Test
    fun details_doesNotShowLotteryOutsideRegistrationPeriod() {
        (lotteryRepository as FakeLotteryRepository).addLottery(
            Lottery(
                lotteryId = "expired-lottery",
                registrationPeriod = Period(start = today - 2, end = today - 1),
                winnerCount = 7,
                waitlistCapacity = 30,
                campsiteIds = listOf("1")
            )
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        openDetails()

        composeRule.onNodeWithText("No lotteries available").assertExists()
        composeRule.onNodeWithText("Winners: 7").assertDoesNotExist()
        composeRule.onNodeWithText("Waitlist spaces: 30").assertDoesNotExist()

        scenario.close()
    }

    @Test
    fun details_showsCampsiteDetailsAlongsideActiveLottery() {
        (lotteryRepository as FakeLotteryRepository).addLottery(
            Lottery(
                lotteryId = "details-lottery",
                registrationPeriod = Period(start = today - 1, end = today + 6000),
                winnerCount = 4,
                waitlistCapacity = 12,
                campsiteIds = listOf("1")
            )
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        openDetails()

        composeRule.onNodeWithTag("details_name").assertIsDisplayed()
        composeRule.onNodeWithTag("details_address").assertIsDisplayed()
        composeRule.onNodeWithText("Winners: 4").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Waitlist spaces: 12").performScrollTo().assertIsDisplayed()

        scenario.close()
    }

    @Test
    fun details_owner_canAccessOwnerFunctionalityOnOwnCampsite() {
        (authRepository as FakeAuthRepository).setCurrentUserId("owner-user")
        (userRepository as FakeUserRepository).addUser(
            User(userId = "owner-user", userType = UserType.CampsiteOwner)
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        openDetails()

        composeRule.onNodeWithText("Edit campsite").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Generate QR Code").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Create lottery").performScrollTo().assertIsDisplayed()

        scenario.close()
    }
}
