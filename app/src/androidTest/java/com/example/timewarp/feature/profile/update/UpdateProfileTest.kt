package com.example.timewarp.feature.profile.update

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeProfileRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ProfileRepository
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

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UpdateProfileTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Before
    fun setUp() {
        hiltRule.inject()

        // User
        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(
            User(userId = "profile-user", userType = UserType.Entrant)
        )

        // Auth
        (authRepository as FakeAuthRepository).setCurrentUserId("profile-user")

        // Profile
        (profileRepository as FakeProfileRepository).clear()
        (profileRepository as FakeProfileRepository).addProfile(
            Profile(
                userId = "profile-user",
                firstName = "Ada",
                lastName = "Lovelace",
                email = "ada@example.com",
                phoneNumber = "1234567890"
            )
        )
    }

    private fun openUpdateProfile(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Account").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Account").performClick()
        composeRule.onNodeWithText("Update profile").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Enter your details").fetchSemanticsNodes().isNotEmpty()
        }
        return scenario
    }

    @Test
    fun updateProfile_loadsExistingProfileValues() {
        val scenario = openUpdateProfile()

        composeRule.onNodeWithTag("updateFirstName").assertIsDisplayed()
        composeRule.onNodeWithText("Ada").assertIsDisplayed()
        composeRule.onNodeWithText("Lovelace").assertIsDisplayed()
        composeRule.onNodeWithText("ada@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("1234567890").assertIsDisplayed()

        scenario.close()
    }

    @Test
    fun updateProfile_requiresRequiredFieldsBeforeSaving() {
        val scenario = openUpdateProfile()

        composeRule.onNodeWithText("Ada").performTextReplacement("")
        composeRule.onNodeWithTag("updateButton")
            .performScrollTo()
            .assertIsNotEnabled()

        scenario.close()
    }

    @Test
    fun updateProfile_savesEditedValuesAndReturnsToAccount() {
        val scenario = openUpdateProfile()

        composeRule.onNodeWithText("Ada").performTextReplacement("Grace")
        composeRule.onNodeWithText("Lovelace").performTextReplacement("Hopper")
        composeRule.onNodeWithText("ada@example.com")
            .performTextReplacement("grace@example.com")
        composeRule.onNodeWithText("1234567890").performTextReplacement("0987654321")
        composeRule.onNodeWithTag("updateButton").performScrollTo().performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Update profile").fetchSemanticsNodes(
                atLeastOneRootRequired = false
            ).isNotEmpty()
        }

        val saved = runBlocking { profileRepository.getProfile("profile-user") }
        assertEquals("Grace", saved?.firstName)
        assertEquals("Hopper", saved?.lastName)
        assertEquals("grace@example.com", saved?.email)
        assertEquals("0987654321", saved?.phoneNumber)

        scenario.close()
    }

    @Test
    fun updateProfile_cancelLeavesProfileUnchanged() {
        val scenario = openUpdateProfile()

        composeRule.onNodeWithText("Ada").performTextReplacement("Changed")
        composeRule.onNodeWithTag("cancelButton").performScrollTo().performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Update profile").fetchSemanticsNodes(
                atLeastOneRootRequired = false
            ).isNotEmpty()
        }
        val unchanged = runBlocking { profileRepository.getProfile("profile-user") }
        assertEquals("Ada", unchanged?.firstName)

        scenario.close()
    }
}
