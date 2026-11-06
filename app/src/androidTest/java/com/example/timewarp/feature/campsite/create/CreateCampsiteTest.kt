package com.example.timewarp.feature.campsite.create

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
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val OWNER_ID = "campsite-owner"

/**
 * Instrumented tests for campsite creation navigation.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateCampsiteTest {
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
            User(userId = OWNER_ID, userType = UserType.CampsiteOwner)
        )
        (authRepository as FakeAuthRepository).setCurrentUserId(OWNER_ID)
    }

    @Test
    fun owner_canOpenCampsiteCreationFromMyCampsites() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("browse_tab_MyCampsites")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag("browse_tab_MyCampsites").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag("register_campsite_button")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithTag("register_campsite_button").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("PITCH/CAMPSITE/CREATE")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithText("PITCH/CAMPSITE/CREATE").assertExists()

        scenario.close()
    }
}
