package com.example.timewarp.feature.moderate

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
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.internal.wait
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AdminSelectionTest {
    @get:
    Rule(order = 0) val hiltRule = HiltAndroidRule(this)

    @get:
    Rule(order = 1) val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    /*
    * This function will start a test with no signed-in user
    */
    @Before
    fun setUp() {
        hiltRule.inject()
        (userRepository as FakeUserRepository).clear()
    }

    /*
    * This function will sign in a user
    */
    private fun singIn_as(userId: String, type: UserType) {
        (authRepository as FakeAuthRepository).setCurrentUserId(userId)
        (userRepository as FakeUserRepository).addUser(User(userId = userId, userType = type))
    }

    /*
    * This function will wait 5 seconds for the tag to appear
    */
    private fun waitFor_Tag(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

    /*
    * This function will wait 5 seconds for the text to appear
    */
    private fun waitFor_Text(text: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    }

    /*
    * This function will open the alerts screen
    */
    private fun openModerate() {
        waitFor_Text("Log")
        composeRule.onNodeWithText("Log").performClick()
        waitFor_Tag("admin_tab_Profiles")
    }

    /*
    * Admin sees the Admin tab
    */
    @Test
    fun adminTab_showAll_SelectionTabs(){
        singIn_as("admin_user", UserType.Admin)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openModerate()
        listOf("Profiles", "Images", "Notifications", "Messages").forEach {
            composeRule.onNodeWithTag("admin_tab_$it").assertIsDisplayed()
        }
        scenario.close()
    }

    /*
    * Will check the Notifications tab and what is inside
    */
    @Test
    fun admin_notificationsTab() {
        singIn_as("admin_user", UserType.Admin)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openModerate()
        composeRule.onNodeWithTag("admin_tab_Notifications").performClick()
        composeRule.onNodeWithText("Notifications: Coming Soon", ignoreCase = true).assertIsDisplayed()
        scenario.close()
    }

    /*
    * Will check the Messages tab and its empty state
    */
    @Test
    fun admin_messagesTab() {
        singIn_as("admin_user", UserType.Admin)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openModerate()
        composeRule.onNodeWithTag("admin_tab_Messages").performClick()
        composeRule.onNodeWithText("No Messages").assertIsDisplayed()
        scenario.close()
    }

    /*
    * Will check to make sure entrant does not have the admin tab
    */
    @Test
    fun entrant_CannotSee_AdminTab() {
        singIn_as("entrant-user", UserType.Entrant)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitFor_Text("Browse")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Log").assertDoesNotExist()
        scenario.close()
    }

    /*
    * Will check to make sure campsite owner does not have the admin tab
    */
    @Test
    fun owner_CannotSee_AdminTab() {
        singIn_as("owner-user", UserType.CampsiteOwner)
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        waitFor_Text("Browse")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Log").assertDoesNotExist()
        scenario.close()
    }

}