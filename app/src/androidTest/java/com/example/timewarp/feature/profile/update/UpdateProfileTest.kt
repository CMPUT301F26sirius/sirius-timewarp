package com.example.timewarp.feature.profile.update

import android.net.Uri
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
import com.example.timewarp.data.repository.FakeImageRepository
import com.example.timewarp.data.repository.FakeProfileRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ImageRepository
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

    @Inject
    lateinit var imageRepository: ImageRepository

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
        (imageRepository as FakeImageRepository).clear()

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
        val imageId = createProfileImageId()
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
        assertEquals(imageId, saved?.imageId)

        scenario.close()
    }

    @Test
    fun updateProfile_removesImageIdWhenPhotoIsRemoved() {
        val imageId = createProfileImageId()
        val scenario = openUpdateProfile()

        composeRule.onNodeWithText("Remove photo")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("updateButton").performScrollTo().performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Update profile").fetchSemanticsNodes(
                atLeastOneRootRequired = false
            ).isNotEmpty()
        }

        val saved = runBlocking { profileRepository.getProfile("profile-user") }
        assertEquals("", saved?.imageId)
        assertEquals(imageId, runBlocking { imageRepository.getImage(imageId) }?.imageId)

        scenario.close()
    }

    @Test
    fun updateProfile_savesSelectedInterests() {
        val scenario = openUpdateProfile()

        composeRule.onNodeWithText("Camping")
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithTag("updateButton").performScrollTo().performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Update profile").fetchSemanticsNodes(
                atLeastOneRootRequired = false
            ).isNotEmpty()
        }

        val saved = runBlocking { profileRepository.getProfile("profile-user") }
        assertEquals(listOf("Camping"), saved?.interests)

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

    private fun createProfileImageId(): String {
        val image = checkNotNull(runBlocking {
            imageRepository.uploadImage(Uri.parse("content://test.images/profile"))
        }) { "Fake image upload should return an image." }
        val profile = checkNotNull(runBlocking {
            profileRepository.getProfile("profile-user")
        })
        runBlocking {
            profileRepository.editProfile(
                userId = profile.userId,
                profile = profile.copy(imageId = image.imageId)
            )
        }
        return image.imageId
    }
}
