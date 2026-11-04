package com.example.timewarp.feature.policy

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the policy link on the registration screen.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PolicyTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createEmptyComposeRule()
    
    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun unauthenticatedUser_canOpenPolicyFromRegistration() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("YOUR DEVICE IS YOUR KEY")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithTag("terms_and_privacy_policy").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/POLICY")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("PITCH/POLICY").assertIsDisplayed()

        scenario.close()
    }
}
