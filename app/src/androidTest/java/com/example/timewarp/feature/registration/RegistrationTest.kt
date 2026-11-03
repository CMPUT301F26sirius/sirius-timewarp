package com.example.timewarp.feature.registration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltAndroidRule
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the startup and registration flows.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RegistrationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        (authRepository as FakeAuthRepository).setCurrentUserId(null)
        (userRepository as FakeUserRepository).clear()
    }

    @Test
    fun unauthenticatedUser_isSentToRegistration_andCanRegisterAsCampsiteOwner() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("YOUR DEVICE IS YOUR KEY").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("registration_user_type").performClick()
        composeRule.onNodeWithText("Campsite Owner").performClick()
        composeRule.onNodeWithTag("registration_button").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/BROWSE").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("PITCH/BROWSE").assertIsDisplayed()
        
        scenario.close()
    }

    @Test
    fun authenticatedUser_isSentToBrowse() {
        val userId = "authenticated-user"
        (authRepository as FakeAuthRepository).setCurrentUserId(userId)
        (userRepository as FakeUserRepository).addUser(User(userId = userId, userType = UserType.Entrant)        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/BROWSE").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("PITCH/BROWSE").assertIsDisplayed()

        scenario.close()
    }
}