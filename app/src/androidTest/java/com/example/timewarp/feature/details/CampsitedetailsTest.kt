package com.example.timewarp.feature.details

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
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CampsitedetailsTest {

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

    /*
    * This function logs a test entrant in and seeds 3 campsites before each test
    */
    @Before
    fun setUp() {
        hiltRule.inject()
        (authRepository as FakeAuthRepository).setCurrentUserId("browse-user")
        (userRepository as FakeUserRepository).addUser(
            User(userId = "browse-user", userType = UserType.Entrant)
        )
        val repository = campsiteRepository as FakeCampsiteRepository
        repository.clear()
        repository.addCampsite(
            Campsite(
            campsiteId = "1",
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

}


