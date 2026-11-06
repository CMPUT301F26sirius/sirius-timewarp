package com.example.timewarp.feature.campsite.create

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
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val OWNER_ID = "campsite-owner"

/** Tests opening the registration form, required input, and cancellation. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateCampsiteTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var campsiteRepository: CampsiteRepository

    private var scenario: ActivityScenario<MainActivity>? = null

    @Before
    fun setUp() {
        hiltRule.inject()
        (campsiteRepository as FakeCampsiteRepository).clear()
        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(
            User(userId = OWNER_ID, userType = UserType.CampsiteOwner)
        )
        (authRepository as FakeAuthRepository).setCurrentUserId(OWNER_ID)
    }

    @After
    fun tearDown() {
        scenario?.close()
    }

    /** An owner can open the form from My Campsites. */
    @Test
    fun owner_canOpenCampsiteCreationFromMyCampsites() {
        openRegistrationScreen()
        composeRule.onNodeWithText("PITCH/CAMPSITE/CREATE").assertIsDisplayed()
    }

    /** Submitting an empty form shows a required-field error without saving. */
    @Test
    fun emptyForm_showsNameError() {
        openRegistrationScreen()
        composeRule.onNodeWithTag("registerCampsiteButton")
            .performScrollTo().performClick()

        composeRule.onNodeWithText("Enter a campsite name.")
            .performScrollTo().assertIsDisplayed()
        val campsites = runBlocking { campsiteRepository.getCampsites() }
        assertTrue(campsites.isNullOrEmpty())
    }

    /** Cancel returns to Browse without creating a campsite. */
    @Test
    fun cancel_returnsToBrowseWithoutSaving() {
        openRegistrationScreen()
        composeRule.onNodeWithText("Cancel registration")
            .performScrollTo().performClick()

        waitForTag("browse_search_field")
        composeRule.onNodeWithText("PITCH/CAMPSITE/CREATE").assertDoesNotExist()
        val campsites = runBlocking { campsiteRepository.getCampsites() }
        assertTrue(campsites.isNullOrEmpty())
    }

    /** Shared steps to launch Browse and open the registration form. */
    private fun openRegistrationScreen() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForTag("browse_tab_MyCampsites")
        composeRule.onNodeWithTag("browse_tab_MyCampsites").performClick()
        waitForTag("register_campsite_button")
        composeRule.onNodeWithTag("register_campsite_button").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("PITCH/CAMPSITE/CREATE")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
