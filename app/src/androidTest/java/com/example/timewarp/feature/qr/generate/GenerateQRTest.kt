package com.example.timewarp.feature.qr.generate

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeProfileRepository
import com.example.timewarp.data.repository.FakeRatingRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.navigation.Screen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GenerateQRTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var campsiteRepository: CampsiteRepository

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var ratingRepository: RatingRepository

    @Inject
    lateinit var lotteryRepository: LotteryRepository

    private val ownerId = "qr-owner"
    private val entrantId = "qr-entrant"
    private val campsiteId = "qr-campsite"
    private val expectedLink = "pitch://${Screen.CampsiteDetails.createRoute(campsiteId)}"

    @Before
    fun setUp() {
        hiltRule.inject()

        // Clear users
        (userRepository as FakeUserRepository).clear()

        // Setup owner profile
        (profileRepository as FakeProfileRepository).clear()
        (profileRepository as FakeProfileRepository).addProfile(
            Profile(userId = ownerId, firstName = "QR", lastName = "Owner")
        )

        // Setup campsites
        (campsiteRepository as FakeCampsiteRepository).clear()
        (campsiteRepository as FakeCampsiteRepository).addCampsite(
            Campsite(
                campsiteId = campsiteId,
                ownerUserId = ownerId,
                name = "QR Test Campsite",
                location = Location(address = "Test location")
            )
        )

        // Setup ratings and lottery
        (ratingRepository as FakeRatingRepository).clear()
        (lotteryRepository as FakeLotteryRepository).clear()
    }

    @Test
    fun owner_canOpenCampsiteDeepLink_andCanGenerateQrCodeWithCorrectLink() {
        // Authenticate as owner
        (authRepository as FakeAuthRepository).setCurrentUserId(ownerId)
        (userRepository as FakeUserRepository).addUser(
            User(userId = ownerId, userType = UserType.CampsiteOwner)
        )

        // Open to campsite with deeplink
        val scenario = ActivityScenario.launch<MainActivity>(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(expectedLink)
            )
        )

        // Generate and check qr link
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Generate QR Code").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Generate QR Code").performClick()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(expectedLink).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(expectedLink).assertIsDisplayed()

        scenario.close()
    }

    @Test
    fun entrant_canViewTheSameCampsite_withoutOwnerControls() {
        // Authenticate as entrant instead of owner
        (authRepository as FakeAuthRepository).setCurrentUserId(entrantId)
        (userRepository as FakeUserRepository).addUser(
            User(userId = entrantId, userType = UserType.Entrant)
        )

        val scenario = ActivityScenario.launch<MainActivity>(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(expectedLink)
            )
        )

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("QR Test Campsite").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("QR Test Campsite").assertIsDisplayed()
        composeRule.onNodeWithText("Generate QR Code").assertDoesNotExist()

        scenario.close()
    }
}
