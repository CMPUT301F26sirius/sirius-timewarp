package com.example.timewarp.feature.campsite.edit

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeImageRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.Pricing
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ImageRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests saving and cancelling edits through the campsite owner's UI. */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EditCampsiteTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var campsiteRepository: CampsiteRepository
    @Inject lateinit var imageRepository: ImageRepository

    private var scenario: ActivityScenario<MainActivity>? = null

    private val campsite = Campsite(
        campsiteId = "edit-test-campsite",
        ownerUserId = "edit-test-owner",
        name = "Pine Ridge Campsite",
        description = "A quiet campsite by the lake.",
        location = Location(address = "Jasper, AB"),
        occupancy = Occupancy(maxGuests = 4),
        pricing = Pricing(costPerNight = 45.0),
        rating = 4.5,
        checkInTime = "14:00",
        checkOutTime = "11:00"
    )

    @Before
    fun setUp() {
        hiltRule.inject()
        (campsiteRepository as FakeCampsiteRepository).apply {
            clear()
            addCampsite(campsite)
        }
        (userRepository as FakeUserRepository).apply {
            clear()
            addUser(User(userId = campsite.ownerUserId, userType = UserType.CampsiteOwner))
        }
        (authRepository as FakeAuthRepository).setCurrentUserId(campsite.ownerUserId)
        (imageRepository as FakeImageRepository).clear()
    }

    @After
    fun tearDown() {
        scenario?.close()
    }

    @Test
    fun save_prefillsAndUpdatesExistingCampsite() {
        val imageId = seedCampsiteImage()
        openEdit()
        val updatedName = "Pine Ridge Lakefront"

        composeRule.onNodeWithTag("campsiteUploadedImage").assertIsDisplayed()
        nameField().performScrollTo().assertTextEquals(campsite.name)
        nameField().performTextReplacement(updatedName)
        closeSoftKeyboard()
        composeRule.onNodeWithText("Save changes").performScrollTo().performClick()

        waitForTag("details_name")
        composeRule.onNodeWithTag("details_name").assertTextEquals(updatedName)
        val stored = runBlocking { campsiteRepository.getCampsites().orEmpty() }
        assertEquals(listOf(campsite.copy(name = updatedName, imageId = imageId)), stored)
    }

    @Test
    fun cancel_discardsChangesAndReturnsToDetails() {
        openEdit()
        nameField().performScrollTo().performTextReplacement("Unsaved name")
        closeSoftKeyboard()
        composeRule.onNodeWithText("Cancel").performScrollTo().performClick()

        waitForTag("details_name")
        composeRule.onNodeWithTag("details_name").assertTextEquals(campsite.name)
        val stored = runBlocking { campsiteRepository.getCampsite(campsite.campsiteId) }
        assertEquals(campsite, stored)
    }

    private fun openEdit() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForTag("browse_tab_MyCampsites")
        composeRule.onNodeWithTag("browse_tab_MyCampsites").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText(campsite.name).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(campsite.name).performClick()
        waitForTag("details_name")
        composeRule.onNodeWithText("Edit campsite").performScrollTo().performClick()
        waitForTag("registerCampsite_name")
        composeRule.onNodeWithText("PITCH/CAMPSITE/EDIT").assertIsDisplayed()
    }

    private fun nameField() = composeRule.onNode(
        hasSetTextAction() and hasAnyAncestor(hasTestTag("registerCampsite_name")),
        useUnmergedTree = true
    )

    private fun seedCampsiteImage(): String {
        val image = checkNotNull(runBlocking {
            imageRepository.uploadImage(Uri.parse("content://test.images/campsite"))
        })
        runBlocking {
            campsiteRepository.editCampsite(
                campsiteId = campsite.campsiteId,
                campsite = campsite.copy(imageId = image.imageId)
            )
        }
        return image.imageId
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}