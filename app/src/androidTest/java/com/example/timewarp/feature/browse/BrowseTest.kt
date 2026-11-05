package com.example.timewarp.feature.browse

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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

/*
* Instrumented tests for the browser screen that covers user stories
* 01.01.01 , 01.01.02, 01.01.03, 01.01.04.
* The test runs against fake repositories. It covers filtering, listing
* and campsite card admin moderator tab.
*/

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BrowseTest {
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
    * This function builds a test campsite with the given id, name, address, and max guests
    */
    private fun campsite(
        id: String,
        name: String,
        address: String,
        guests: Int

    ) =
        Campsite(
            campsiteId = id,
            name = name,
            rating = 4.5,
            location = Location(address = address),
            occupancy = Occupancy(maxGuests = guests)
        )

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
        repository.addCampsite(campsite("1", "Bear Creek", "Dryton Valley, AB", 8))
        repository.addCampsite(campsite("2", "Crimson lake", "Rocky Mountain House, AB", 4))
        repository.addCampsite(campsite("3", "Jasper Pines", "Jasper Pines, AB", 8))
    }

    /*
    * This function waits for 5 seconds for the text to appear on the screen
    */
    private fun waitForText(text: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    }

    /*
    * US 01.01.02: Filtering by 6 guests. Hides teh campsite that has 4 guests
    */
    @Test
    fun filter_guests_010102() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Bear Creek")
        composeRule.onNodeWithTag("browse_filter_button").performClick()
        repeat(6) {composeRule.onNodeWithTag("filter_guests_plus").performClick()}
        composeRule.onNodeWithTag("filter_apply").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Crimson lake").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText("Bear Creek").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.01.02: Filtering by location. Keeps only the campsite whose address matches
    */
    @Test
    fun filter_locationMatchAddress_010102() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Bear Creek")
        composeRule.onNodeWithTag("browse_filter_button").performClick()
        composeRule.onNodeWithTag("filter_location").performTextInput("Jasper")
        composeRule.onNodeWithTag("filter_apply").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Bear Creek").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText("Jasper Pines").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.01.02: This function clears all the filters and restores the full list
    */
    @Test
    fun filter_clearAll_010102() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Bear Creek")
        composeRule.onNodeWithTag("browse_filter_button").performClick()
        repeat(6) {composeRule.onNodeWithTag("filter_guests_plus").performClick()}
        composeRule.onNodeWithTag("filter_apply").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Crimson lake").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag("browse_filter_button").performClick()
        composeRule.onNodeWithTag("filter_clear").performClick()
        waitForText("Crimson lake")
        composeRule.onNodeWithText("Bear Creek").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.01.02: This function opens the dates button data range picker
    */
    @Test
    fun filter_datesPicker_010102() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Bear Creek")
        composeRule.onNodeWithTag("browse_filter_button").performClick()
        composeRule.onNodeWithTag("filter_dates").performClick()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.01.03: This shows all the campsites in the list
    */
    @Test
    fun show_allCampsites_010103() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Bear Creek")
        composeRule.onNodeWithText("Bear Creek").assertIsDisplayed()
        composeRule.onNodeWithText("Crimson lake").assertIsDisplayed()
        composeRule.onNodeWithText("Jasper Pines").assertIsDisplayed()
        scenario.close()
    }

    /*
    * US 01.01.04: This shows a campsite card with its name, address, max guests and rating
    */
    @Test
    fun show_Name_Address_Guests_Rating_Card_010104() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Crimson lake")
        composeRule.onNodeWithText("Crimson lake").assertIsDisplayed()
        composeRule.onNodeWithText("Rocky Mountain House, AB").assertIsDisplayed()
        composeRule.onNodeWithText("Maximum Occupancy: 4").assertIsDisplayed()
        composeRule.onAllNodesWithText("Rating: 4.5").onFirst().assertIsDisplayed()
        scenario.close()
    }


    /*
    * US 01.01.04: This shows opening a campsite card by clicking and it opens its details
    */
    @Test
    fun openCampsiteDetails_010104(){
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Crimson lake")
        composeRule.onNodeWithText("Crimson lake").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("details_name").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("details_name").assertIsDisplayed()
        scenario.close()
    }


    /*
    * US 03.04.01: This shows the admin moderator tab which shows the campsites without filter button
    */
    @Test
    fun admin_ModeratorTab_noFiltersButton_030401() {
        (authRepository as FakeAuthRepository).setCurrentUserId("admin")
        (userRepository as FakeUserRepository).addUser(
            User(userId = "admin", userType = UserType.Admin)
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Bear Creek")
        composeRule.onAllNodesWithText("Moderate").onFirst().performClick()
        waitForText("Bear Creek")
        composeRule.onNodeWithTag("browse_filter_button").assertDoesNotExist()
        composeRule.onNodeWithText("Bear Creek").assertIsDisplayed()
        scenario.close()
    }


}