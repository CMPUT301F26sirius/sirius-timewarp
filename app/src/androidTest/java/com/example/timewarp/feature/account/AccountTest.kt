package com.example.timewarp.feature.account

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
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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
class AccountTest {
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
        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(
            User(
                userId = "account-user",
                userType = UserType.Entrant,
                notificationsEnabled = true,
            )
        )
        (authRepository as FakeAuthRepository).setCurrentUserId("account-user")
    }

    @Test
    fun notificationToggle_updatesUserPreference() {
        val scenario = openAccount()

        composeRule.onNodeWithText("Notifications: On").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Notifications: Off")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        assertEquals(
            false,
            runBlocking { userRepository.getUser("account-user")?.notificationsEnabled }
        )

        composeRule.onNodeWithText("Notifications: Off").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Notifications: On")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        assertEquals(
            true,
            runBlocking { userRepository.getUser("account-user")?.notificationsEnabled }
        )

        scenario.close()
    }

    private fun openAccount(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Account")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Account").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Notifications: On")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        return scenario
    }
}