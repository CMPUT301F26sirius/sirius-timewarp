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
import com.example.timewarp.data.repository.FakeImageRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Image
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ImageRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AdminImagesTest {
    @get:
    Rule(order = 0) val hiltRule = HiltAndroidRule(this)

    @get:
    Rule(order = 1) val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var imageRepository: ImageRepository


    /*
    * This function will sing in as an admin and will seed two uploaded images
    */
    @Before
    fun setUp() {
        hiltRule.inject()
        (userRepository as FakeUserRepository).clear()
        (imageRepository as FakeImageRepository).clear()
        (authRepository as FakeAuthRepository).setCurrentUserId("admin-user")
        (userRepository as FakeUserRepository).addUser(
            User(userId = "admin-user", userType = UserType.Admin)
        )

        val images = imageRepository as FakeImageRepository
        images.addImage(
            Image(
                uploadingUserId = "entrant-1",
                imageId = "image-1",
                url = "u1",
                storagePath = "fake/image/image-1"
                )
        )

        images.addImage(
            Image(
                uploadingUserId = "owner-1",
                imageId = "image-2",
                url = "u2",
                storagePath = "fake/image/image-2"
            )
        )
    }

    /*
    * This function will wait 5 seconds for the tag to appear
    */
    private fun waitFor_Tag(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

    /*
    * This function will wait 5 seconds for the tag to disappear
    */
    private fun waitFor_TabGone(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()
    }

    /*
    * This function will wait 5 seconds for the text to appear
    */
    private fun waitFor_Text(text: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    }

    /*
    * This function will open the admin section and then Images tab
    */
    private fun openImagesTab() {
        waitFor_Text("Log")
        composeRule.onNodeWithText("Log").performClick()
        waitFor_Text("Images")
        composeRule.onNodeWithText("Images").performClick()
        waitFor_Tag("admin_image_item_image-1")
    }

    /*
    * This test will show every uploaded image in the Images tab
    */
    @Test
    fun admin_imagesTab_ShowListImages() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openImagesTab()
        composeRule.onNodeWithTag("admin_image_item_image-1").assertIsDisplayed()
        composeRule.onNodeWithTag("admin_image_item_image-2").assertIsDisplayed()
        scenario.close()
    }

    /*
    * This test will delete image from the list
    */
    @Test
    fun admin_deleteImages() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openImagesTab()
        composeRule.onNodeWithTag("admin_image_item_image-1").performClick()
        waitFor_Tag("confirm_admin_action")
        composeRule.onNodeWithTag("confirm_admin_action").performClick()

        waitFor_TabGone("admin_image_item_image-1")
        runBlocking {
            TestCase.assertNull(imageRepository.getImage("image-1"))
        }
        composeRule.onNodeWithTag("admin_image_item_image-2").assertIsDisplayed()
        scenario.close()
    }

    @Test
    fun admin_cancelDelete() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openImagesTab()
        composeRule.onNodeWithTag("admin_image_delete_image-1").performClick()
        waitFor_Tag("confirm_admin_action")
        composeRule.onNodeWithText("cancel").performClick()
        waitFor_TabGone("confirm_admin_action")
        composeRule.onNodeWithTag("admin_image_item_image-1").assertIsDisplayed()
        runBlocking {
            TestCase.assertNotNull(imageRepository.getImage("image-1"))
        }
        scenario.close()
    }

    @Test
    fun admin_selectAll_images() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openImagesTab()
        composeRule.onNodeWithTag("admin_images_select_all").performClick()
        waitFor_Text("Delete selected (2)")
        composeRule.onNodeWithText("Unselect all").assertIsDisplayed()

        composeRule.onNodeWithTag("admin_images_select_all").performClick()
        waitFor_TabGone("admin_images_delete_selected")
        composeRule.onNodeWithText("Select all").assertIsDisplayed()
        scenario.close()
    }

    @Test
    fun admin_deleteSelected_OnlyRemoveSelectedImages() {
        (imageRepository as FakeImageRepository).addImage(
            Image(
                uploadingUserId = "owner-1",
                imageId = "image-3",
                url = "u3",
                storagePath = "fake/image/image-3"
            )
        )
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openImagesTab()

        composeRule.onNodeWithTag("admin_image_select_image-1").performClick()
        composeRule.onNodeWithTag("admin_image_select_image-2").performClick()
        waitFor_Text("Delete selected (2)")
        composeRule.onNodeWithTag("admin_images_delete_selected").performClick()
        waitFor_Tag("confirm_delete_selected")
        composeRule.onNodeWithTag("confirm_delete_selected").performClick()

        waitFor_TabGone("admin_image_item_image-1")
        waitFor_TabGone("admin_image_item_image-2")
        composeRule.onNodeWithTag("admin_image_item_image-3").assertIsDisplayed()
        runBlocking {
            TestCase.assertNull(imageRepository.getImage("image-1"))
            TestCase.assertNull(imageRepository.getImage("image-2"))
            TestCase.assertNotNull(imageRepository.getImage("image-3"))
        }
        scenario.close()
    }
}